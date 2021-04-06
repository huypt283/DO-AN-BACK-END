package com.phamthehuy.doan.service.impl;

import com.google.common.primitives.Booleans;
import com.phamthehuy.doan.dao.*;
import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.dto.input.ArticleInsertDTO;
import com.phamthehuy.doan.model.dto.input.ArticleUpdateDTO;
import com.phamthehuy.doan.model.dto.input.RoommateDTO;
import com.phamthehuy.doan.model.dto.output.ArticleOutputDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.entity.*;
import com.phamthehuy.doan.service.CustomerArticleService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomerArticleServiceImpl implements CustomerArticleService {
    final
    ArticleRepository articleRepository;
    final
    StaffArticleRepository staffArticleRepository;
    final
    CustomerRepository customerRepository;
    final
    WardRepository wardRepository;
    final
    Helper helper;
    final
    TransactionRepository transactionRepository;

    public CustomerArticleServiceImpl(ArticleRepository articleRepository, StaffArticleRepository staffArticleRepository, CustomerRepository customerRepository, WardRepository wardRepository, Helper helper, TransactionRepository transactionRepository) {
        this.articleRepository = articleRepository;
        this.staffArticleRepository = staffArticleRepository;
        this.customerRepository = customerRepository;
        this.wardRepository = wardRepository;
        this.helper = helper;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<ArticleOutputDTO> listArticle(String email, String sort, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer page, Integer limit) {
        List<Article> articleList =
                articleRepository.findCustomAndEmail(email, sort, start, end, ward, district, city,
                        roommate, status, vip, search, minAcreage, maxAcreage, page, limit);
        List<ArticleOutputDTO> articleOutputDTOList = new ArrayList<>();
        if (articleList.size() > 0) {
            for (Article article : articleList) {
                articleOutputDTOList.add(convertToOutputDTO(article));
            }
        }
        return articleOutputDTOList;
    }

    @Override
    public ArticleOutputDTO insertArticle(String email, ArticleInsertDTO articleInsertDTO)
            throws CustomException {
        System.out.println("email: " + email);
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new CustomException("Khách hàng đăng bài không hợp lệ");

        //kiểm tra và trừ tiền
        Integer money = null;
        int priceDay = articleInsertDTO.getVip() ? 10000 : 2000;
        int priceWeek = articleInsertDTO.getVip() ? 63000 : 12000;
        int priceMonth = articleInsertDTO.getVip() ? 240000 : 48000;
        switch (articleInsertDTO.getType()) {
            case "day":
                money = articleInsertDTO.getNumber() * priceDay;
                break;
            case "week":
                money = articleInsertDTO.getNumber() * priceWeek;
                break;
            case "month":
                money = articleInsertDTO.getNumber() * priceMonth;
                break;
            default:
                throw new CustomException("Type của thời gian không hợp lệ");
        }
        if (customer.getAccountBalance() < money)
            throw new CustomException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán đăng bài: " + money + " VNĐ cho bài đăng: " + articleInsertDTO.getTitle();

        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Article article = modelMapper.map(articleInsertDTO, Article.class);

            com.phamthehuy.doan.model.entity.Service service = new com.phamthehuy.doan.model.entity.Service();
            service.setElectricPrice(articleInsertDTO.getElectricPrice());
            service.setWaterPrice(articleInsertDTO.getWaterPrice());
            service.setWifiPrice(articleInsertDTO.getWifiPrice());
            article.setService(service);

            RoommateDTO roommateDTO = articleInsertDTO.getRoommateDTO();
            Roommate roommate = null;
            if (roommateDTO != null)
                roommate = modelMapper.map(roommateDTO, Roommate.class);
            article.setRoommate(roommate);

            Optional<Ward> wardOptional = wardRepository.findById(articleInsertDTO.getWardId());
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id không hợp lệ");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            article.setDeleted(null);

            Customer newCustomer = customerRepository.save(customer);
            article.setCustomer(newCustomer);
            creatTransactionPay(money, newCustomer, description);
            return convertToOutputDTO(articleRepository.save(article));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Đăng bài thất bại");
        }
    }

    public void creatTransactionPay(Integer amount, Customer customer, String description) {
        Transaction transaction = new Transaction();
        transaction.setType(false);
        transaction.setAmount(amount);
        transaction.setTimeCreated(new Date());
        transaction.setCustomer(customer);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    @Override
    public ArticleOutputDTO updateArticle(String email, ArticleUpdateDTO articleUpdateDTO,
                                          Integer id) throws CustomException {
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) throw new CustomException("Không tìm thấy khách hàng");
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            Article article = articleRepository.findByArticleId(id);
            if (article == null) throw new CustomException("Bài đăng id không hợp lệ");
            if (customer != article.getCustomer())
                throw new CustomException("Khách hàng không hợp lệ");

            article.setTitle(articleUpdateDTO.getTitle());
            article.setImage(articleUpdateDTO.getImage());
            article.setRoomPrice(articleUpdateDTO.getRoomPrice());
            article.setDescription(articleUpdateDTO.getDescription());
            article.setVip(articleUpdateDTO.getVip());

            article.setAddress(articleUpdateDTO.getAddress());
            article.setAcreage(articleUpdateDTO.getAcreage());
            article.setVideo(articleUpdateDTO.getVideo());

            com.phamthehuy.doan.model.entity.Service service = article.getService();
            service.setElectricPrice(articleUpdateDTO.getElectricPrice());
            service.setWaterPrice(articleUpdateDTO.getWaterPrice());
            service.setWifiPrice(articleUpdateDTO.getWifiPrice());
            article.setService(service);

            RoommateDTO roommateDTO = articleUpdateDTO.getRoommateDTO();
            if (roommateDTO != null) {
                Roommate roommate = article.getRoommate();
                roommate.setDescription(roommateDTO.getDescription());
                roommate.setGender(roommateDTO.getGender());
                roommate.setQuantity(roommateDTO.getQuantity());
                article.setRoommate(roommate);
            } else article.setRoommate(null);


            Optional<Ward> wardOptional = wardRepository.findById(articleUpdateDTO.getWardId());
            if (!wardOptional.isPresent()) throw new CustomException("Ward Id không hợp lệ");
            article.setWard(wardOptional.get());

            article.setUpdateTime(new Date());
            if (article.getDeleted() != null && article.getDeleted())
                article.setDeleted(null);

            return convertToOutputDTO(articleRepository.save(article));
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Cập nhật bài thất bại");
        }
    }

    @Override
    public Message hiddenArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");
        article.setDeleted(true);

        article.setExpTime(null);

        articleRepository.save(article);
        return new Message("Ẩn bài đăng id: " + id + " thành công");
    }

    @Override
    public Message deleteArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");
        articleRepository.delete(article);
        return new Message("Xóa bài đăng id: " + id + " thành công");
    }

    @Override
    public Message extensionExp(String email, Integer id, Integer days, String type) throws CustomException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");
        else if (article.getDeleted())
            throw new CustomException("Gia hạn chỉ áp dụng với bài đăng đã được duyệt");

        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");

        //kiểm tra và trừ tiền
        Integer money = null;
        int priceDay = article.getVip() ? 10000 : 2000;
        int priceWeek = article.getVip() ? 63000 : 12000;
        int priceMonth = article.getVip() ? 240000 : 48000;
        if (type.equals("day")) {
            money = days * priceDay;
        } else if (type.equals("week")) {
            money = days * priceWeek;
        } else if (type.equals("month")) {
            money = days * priceMonth;
        } else throw new CustomException("Type của thời gian không hợp lệ");
        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new CustomException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán gia hạn: " + money + " VNĐ cho bài đăng: " + article.getTitle();

        //tạo thời hạn
        if (article.getType().equals("day")) {
            article.setExpTime(helper.addDayForDate(days, article.getExpTime()));
        } else if (article.getType().equals("week") || article.getType().equals("month")) {
            Integer numberDay = helper.calculateDays(days, type, article.getExpTime());
            article.setExpTime(helper.addDayForDate(numberDay, article.getExpTime()));
        } else throw new CustomException("Type của bài đăng bị sai");

        Customer newCustomer = customerRepository.save(customer);
        creatTransactionPay(money, newCustomer, description);
        articleRepository.save(article);
        return new Message("Gia hạn bài đăng id: " + id + " thành công");
    }

    @Override
    public Message postOldArticle(String email, Integer id, Integer days, String type) throws CustomException {
        Article article = articleRepository.findByDeletedTrueAnAndArticleId(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại, hoặc đang không bị ẩn");
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");

        //kiểm tra và trừ tiền
        Integer money = null;
        int priceDay = article.getVip() ? 10000 : 2000;
        int priceWeek = article.getVip() ? 63000 : 12000;
        int priceMonth = article.getVip() ? 240000 : 48000;
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
                throw new CustomException("Type của thời gian không hợp lệ");
        }
        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new CustomException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        String description = "Thanh toán đăng lại bài: " + money + " VNĐ cho bài đăng: " + article.getTitle();

        article.setDeleted(null);

        article.setNumber(days);
        article.setType(type);

        Customer newCustomer = customerRepository.save(customer);
        creatTransactionPay(money, newCustomer, description);
        articleRepository.save(article);
        return new Message("Đăng lại bài đăng đã ẩn id: " + id + " thành công");
    }

    @Override
    public ArticleOutputDTO detailArticle(String email, Integer id) throws CustomException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new CustomException("Bài đăng với id: " + id + " không tồn tại");

        System.out.println("email: " + email);
        System.out.println("customer: " + article.getCustomer().getEmail());
        if (!email.equals(article.getCustomer().getEmail()))
            throw new CustomException("Khách hàng không hợp lệ");

        return convertToOutputDTO(article);
    }

    public ArticleOutputDTO convertToOutputDTO(Article article) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        ArticleOutputDTO articleOutputDTO = modelMapper.map(article, ArticleOutputDTO.class);
        articleOutputDTO.setCreateTime(article.getTimeCreated().getTime());
        articleOutputDTO.setLastUpdateTime(article.getUpdateTime().getTime());
        if (article.getDeleted() != null) {
            if (article.getDeleted())
                articleOutputDTO.setStatus("Đã ẩn");
            else
                articleOutputDTO.setStatus("Đang đăng");
        } else articleOutputDTO.setStatus("Chưa duyệt");

        StaffArticle staffArticle = staffArticleRepository.
                findFirstByArticle_ArticleId(article.getArticleId(), Sort.by("time").descending());


        if (staffArticle != null && article.getDeleted() != null) {
            Map<String, String> moderator = new HashMap<>();
            moderator.put("staffId", staffArticle.getStaff().getStaffId() + "");
            moderator.put("name", staffArticle.getStaff().getName());
            moderator.put("email", staffArticle.getStaff().getEmail());
            articleOutputDTO.setModerator(moderator);
        }

        Map<String, String> customer = new HashMap<>();
        customer.put("customerId", article.getCustomer().getCustomerId() + "");
        customer.put("name", article.getCustomer().getName());
        customer.put("email", article.getCustomer().getEmail());
        customer.put("phone", article.getCustomer().getPhone());
        articleOutputDTO.setCustomer(customer);

        if (article.getDeleted() != null && !article.getDeleted()) {
            articleOutputDTO.
                    setExpDate(article.getExpTime().getTime());
        }

        Map<String, String> location = new HashMap<>();
        location.put("wardId", article.getWard().getWardId() + "");
        location.put("wardName", article.getWard().getWardName());
        location.put("districtId", article.getWard().getDistrict().getDistrictId() + "");
        location.put("districtName", article.getWard().getDistrict().getDistrictName());
        location.put("cityId", article.getWard().getDistrict().getCity().getCityId() + "");
        location.put("cityName", article.getWard().getDistrict().getCity().getCityName());
        articleOutputDTO.setLocation(location);

        return articleOutputDTO;
    }
}
