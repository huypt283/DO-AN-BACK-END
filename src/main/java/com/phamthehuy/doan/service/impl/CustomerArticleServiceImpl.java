package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.*;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.ArticleInsertRequest;
import com.phamthehuy.doan.model.request.ArticleUpdateRequest;
import com.phamthehuy.doan.model.request.RoommateRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.ArticleRepository;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.TransactionRepository;
import com.phamthehuy.doan.repository.WardRepository;
import com.phamthehuy.doan.service.CustomerArticleService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerArticleServiceImpl implements CustomerArticleService {
    @Autowired
    private ArticleServiceImpl articleService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private WardRepository wardRepository;
    @Autowired
    private Helper helper;
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public List<ArticleResponse> listArticleNotHidden(Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, Boolean vip, String search, Integer minAcreage, Integer maxAcreage) throws Exception {
        List<Article> articles = articleRepository.findCustomNotHidden(start, end, ward, district, city,
                roommate, vip, search, minAcreage, maxAcreage);

        return articles.stream().map(articleService::convertToOutputDTO).collect(Collectors.toList());
    }

    @Override
    public List<ArticleResponse> listArticleByEmail(String email, String sort, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer page, Integer limit) {
        List<Article> articles =
                articleRepository.findCustomByEmail(email, sort, start, end, ward, district, city,
                        roommate, status, vip, search, minAcreage, maxAcreage, page, limit);
        List<ArticleResponse> articleResponseList = new ArrayList<>();

        return articles.stream().map(articleService::convertToOutputDTO).collect(Collectors.toList());
    }

    @Override
    public ArticleResponse insertArticle(String email, ArticleInsertRequest articleInsertRequest)
            throws BadRequestException {
        System.out.println("email: " + email);
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new BadRequestException("Khách hàng đăng bài không hợp lệ");

        //kiểm tra và trừ tiền
        Integer money = null;
        int priceDay = articleInsertRequest.getVip() ? 10000 : 2000;
        int priceWeek = articleInsertRequest.getVip() ? 60000 : 12000;
        int priceMonth = articleInsertRequest.getVip() ? 200000 : 40000;

        Integer days = articleInsertRequest.getDays();
        switch (articleInsertRequest.getTimeType()) {
            case "day":
                money = days * priceDay;
                break;
            case "week":
                money = days * priceWeek;
                break;
            case "month":
                money = days * priceMonth;
                break;
            default:
                throw new BadRequestException("Loại thời gian không hợp lệ");
        }
        if (customer.getAccountBalance() < money)
            throw new BadRequestException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán đăng bài: " + money + " VNĐ cho bài đăng: " + articleInsertRequest.getTitle();

        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Article article = modelMapper.map(articleInsertRequest, Article.class);

            RoomService roomService = new RoomService();
            roomService.setElectricPrice(articleInsertRequest.getElectricPrice());
            roomService.setWaterPrice(articleInsertRequest.getWaterPrice());
            roomService.setWifiPrice(articleInsertRequest.getWifiPrice());
            article.setRoomService(roomService);

            RoommateRequest roommateRequest = articleInsertRequest.getRoommateRequest();
            Roommate roommate = null;
            if (roommateRequest != null)
                roommate = modelMapper.map(roommateRequest, Roommate.class);
            article.setRoommate(roommate);

            Optional<Ward> wardOptional = wardRepository.findById(articleInsertRequest.getWardId());
            if (!wardOptional.isPresent()) throw new BadRequestException("Ward id không hợp lệ");
            article.setWard(wardOptional.get());

            article.setTimeCreated(new Date());
            article.setDeleted(null);

            Customer newCustomer = customerRepository.save(customer);
            article.setCustomer(newCustomer);
            createTransactionPay(money, newCustomer, description);

            article = articleRepository.save(article);

            return articleService.convertToOutputDTO(article);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException("Đăng bài thất bại");
        }
    }

    @Override
    public ArticleResponse updateArticle(String email, ArticleUpdateRequest articleUpdateRequest,
                                         Integer id) throws BadRequestException {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null)
            throw new BadRequestException("Không tìm thấy khách hàng");
        try {
            Article article = articleRepository.findByArticleId(id);
            if (article == null)
                throw new BadRequestException("Không tìm thấy bài đăng");
            if (customer != article.getCustomer())
                throw new BadRequestException("Khách hàng không hợp lệ");

            BeanUtils.copyProperties(articleUpdateRequest, article);
//            RoomService roomService = article.getRoomService();
//            roomService.setElectricPrice(articleUpdateRequest.getElectricPrice());
//            roomService.setWaterPrice(articleUpdateRequest.getWaterPrice());
//            roomService.setWifiPrice(articleUpdateRequest.getWifiPrice());
//            article.setRoomService(roomService);

            RoommateRequest roommateRequest = articleUpdateRequest.getRoommateRequest();
            if (roommateRequest != null) {
                Roommate roommate = article.getRoommate() != null ? article.getRoommate() : new Roommate();
                BeanUtils.copyProperties(roommateRequest, roommate);
                article.setRoommate(roommate);
            } else
                article.setRoommate(null);

            Optional<Ward> wardOptional = wardRepository.findById(articleUpdateRequest.getWardId());
            if (!wardOptional.isPresent())
                throw new BadRequestException("Ward id không hợp lệ");
            article.setWard(wardOptional.get());

            article.setTimeUpdated(new Date());

            if (article.getDeleted() != null && article.getDeleted())
                article.setDeleted(null);

            article = articleRepository.save(article);

            return articleService.convertToOutputDTO(article);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(e.getMessage());
        }
    }

    @Override
    public MessageResponse deleteArticle(String email, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new BadRequestException("Khách hàng không hợp lệ");
        articleRepository.delete(article);
        return new MessageResponse("Xóa bài đăng id: " + id + " thành công");
    }

    @Override
    public MessageResponse extensionExp(String email, Integer id, Integer days, String type) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");
        else if (article.getDeleted())
            throw new BadRequestException("Gia hạn chỉ áp dụng với bài đăng đã được duyệt");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new BadRequestException("Khách hàng không hợp lệ");

        //kiểm tra và trừ tiền
        Integer money;
        int priceDay = article.getVip() ? 10000 : 2000;
        int priceWeek = article.getVip() ? 60000 : 12000;
        int priceMonth = article.getVip() ? 200000 : 40000;

        switch (type) {
            case "day":
                money = days * priceDay;
                break;
            case "week":
                money = days * priceWeek;
                break;
            case "month":
                money = days * priceMonth;
                break;
            default:
                throw new BadRequestException("Loại thời gian không hợp lệ");
        }

        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new BadRequestException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán gia hạn: " + money + " VNĐ cho bài đăng: " + article.getTitle();

        //tạo thời hạn
        article.setExpTime(helper.addDayForDate(days, article.getExpTime()));

        customer = customerRepository.save(customer);

        createTransactionPay(money, customer, description);
        articleRepository.save(article);
        return new MessageResponse("Gia hạn bài đăng id: " + id + " thành công");
    }

    @Override
    public MessageResponse hideArticle(String email, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new BadRequestException("Khách hàng không hợp lệ");
        article.setDeleted(true);

        articleRepository.save(article);
        return new MessageResponse("Ẩn bài đăng id: " + id + " thành công");
    }

    @Override
    public MessageResponse showArticle(String email, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null) {
            throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");
        } else {
            if (!article.getDeleted())
                throw new BadRequestException("Bài đăng với id: " + id + " không bị ẩn");
            if (article.getExpTime().after(new Date()))
                throw new BadRequestException("Bài đăng với id: " + id + " đã hết hạn");
        }

        if (!email.equals(article.getCustomer().getEmail()))
            throw new BadRequestException("Khách hàng không hợp lệ");

//        //kiểm tra và trừ tiền
//        Integer money = null;
//        int priceDay = article.getVip() ? 10000 : 2000;
//        int priceWeek = article.getVip() ? 60000 : 12000;
//        int priceMonth = article.getVip() ? 200000 : 40000;
//        switch (type) {
//            case "day":
//                money = days * priceDay;
//                break;
//            case "week":
//                money = days * priceWeek;
//                break;
//            case "month":
//                money = days * priceMonth;
//                break;
//            default:
//                throw new CustomException("Type của thời gian không hợp lệ");
//        }
//        Customer customer = article.getCustomer();
//        if (customer.getAccountBalance() < money)
//            throw new CustomException("Số tiền trong tài khoản không đủ");
//        customer.setAccountBalance(customer.getAccountBalance() - money);
//
//        String description = "Thanh toán đăng lại bài: " + money + " VNĐ cho bài đăng: " + article.getTitle();

        article.setDeleted(false);

        articleRepository.save(article);
        return new MessageResponse("Hiển thị lại bài đăng đã ẩn id: " + id + " thành công");
    }

    @Override
    public ArticleResponse detailArticle(String email, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");

        System.out.println("email: " + email);
        System.out.println("customer: " + article.getCustomer().getEmail());
        if (!email.equals(article.getCustomer().getEmail()))
            throw new BadRequestException("Khách hàng không hợp lệ");

        return articleService.convertToOutputDTO(article);
    }

    private void createTransactionPay(Integer amount, Customer customer, String description) {
        Transaction transaction = new Transaction();
        transaction.setType(false);
        transaction.setAmount(amount);
        transaction.setTimeCreated(new Date());
        transaction.setCustomer(customer);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
}
