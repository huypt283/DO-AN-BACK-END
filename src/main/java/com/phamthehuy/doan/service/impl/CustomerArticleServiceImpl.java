package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.*;
import com.phamthehuy.doan.exception.*;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.ArticleRepository;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.TransactionRepository;
import com.phamthehuy.doan.repository.WardRepository;
import com.phamthehuy.doan.service.CustomerArticleService;
import com.phamthehuy.doan.util.SlugUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<ArticleResponse> listArticleNotHidden(String roomType, String title,
                                                      Integer ward, Integer district, Integer city,
                                                      Integer minPrice, Integer maxPrice,
                                                      Integer minAcreage, Integer maxAcreage) throws Exception {
        List<Article> articles = articleRepository.findCustomNotHidden(roomType, title, ward, district, city, minPrice, maxPrice, minAcreage, maxAcreage);
        return articles.stream().map(articleService::convertToArticleResponse).collect(Collectors.toList());
    }

    @Override
    public ArticleResponse getArticleBySlug(String slug) throws Exception {
        Article article = articleRepository.findBySlug(slug);
        if (article == null) {
            throw new NotFoundException("Bài viết không tồn tại");
        }
        if (article.getDeleted() == null || article.getDeleted()) {
            throw new NotFoundException("Bài viết bị ẩn hoặc chưa được duyệt");
        }
        return articleService.convertToArticleResponse(article);
    }

    @Override
    public List<ArticleResponse> getListNewArticle(Integer page, Integer limit) throws Exception {
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest((page - 1) * limit, limit, Sort.by("timeUpdated").descending().and(Sort.by("timeCreated").descending()));
        return articleRepository.findByDeletedFalse(pageable).stream().map(articleService::convertToArticleResponse).collect(Collectors.toList());
    }

    @Override
    public List<ArticleResponse> listCurrentUserArticleByEmail(Long start, Long end, String roomType,
                                                               Integer ward, Integer district, Integer city,
                                                               String status, Boolean vip, String title,
                                                               Integer minAcreage, Integer maxAcreage,
                                                               UserDetails currentUser) throws Exception {
        List<Article> articles = articleRepository.findCustomByEmail(start, end, roomType, ward, district, city,
                status, vip, title, minAcreage, maxAcreage, currentUser);

        return articles.stream().map(articleService::convertToArticleResponse).collect(Collectors.toList());
    }

    @Override
    public ArticleResponse detailArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");

        if (!currentUser.getUsername().equals(article.getCustomer().getEmail()))
            throw new AccessDeniedException("Bạn không có quyền xem chi tiết bài viết này");

        return articleService.convertToArticleResponse(article);
    }

    @Transactional
    @Override
    public ArticleResponse insertArticle(UserDetails currentUser, ArticleInsertRequest articleInsertRequest)
            throws Exception {
        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
        if (customer == null)
            throw new NotFoundException("Không tìm thấy tài khoản");
        else if (!customer.getEnabled())
            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
        else if (customer.getDeleted())
            throw new AccessDeniedException("Tài khoản này đang bị khoá");

        //kiểm tra và trừ tiền
        Integer money = 0;
        int priceDay = articleInsertRequest.getVip() ? 10000 : 2000;
        int priceWeek = articleInsertRequest.getVip() ? 60000 : 12000;
        int priceMonth = articleInsertRequest.getVip() ? 200000 : 40000;

        Integer times = articleInsertRequest.getTimes(), days = 0;
        String timeType = articleInsertRequest.getTimeType();
        switch (timeType) {
            case "day":
                days = times;
                money = times * priceDay;
                break;
            case "week":
                days = helper.calculateDays(times, timeType, new Date());
                money = times * priceWeek;
                break;
            case "month":
                days = helper.calculateDays(times, timeType, new Date());
                money = times * priceMonth;
                break;
            default:
                throw new BadRequestException("Loại thời gian không hợp lệ");
        }
        if (customer.getAccountBalance() < money)
            throw new ConflictException("Số dư trong tài khoản không đủ");

        customer.setAccountBalance(customer.getAccountBalance() - money);

        try {
            Article article = new Article();
            BeanUtils.copyProperties(articleInsertRequest, article);
            article.setDays(days);
            article.setRoomType(articleInsertRequest.getRoomType().toString());

            RoomService roomService = new RoomService();
            BeanUtils.copyProperties(articleInsertRequest, roomService);
            article.setRoomService(roomService);
            article.setSlug(SlugUtil.makeSlug(article.getTitle()) + "-" + System.currentTimeMillis());

            RoommateRequest roommateRequest = articleInsertRequest.getRoommateRequest();
            Roommate roommate = new Roommate();
            if (roommateRequest != null) {
                BeanUtils.copyProperties(roommateRequest, roommate);
                article.setRoommate(roommate);
            }

            Optional<Ward> ward = wardRepository.findById(articleInsertRequest.getWardId());
            if (!ward.isPresent())
                throw new BadRequestException("Mã phường/xã không hợp lệ");
            article.setWard(ward.get());

            article.setTimeCreated(new Date());
            article.setDeleted(null);

            customer = customerRepository.save(customer);
            article.setCustomer(customer);

            article = articleRepository.save(article);

            String description = "Thanh toán " + money + " VNĐ cho bài đăng số: " + article.getArticleId() + " - " + article.getTitle();
            createTransactionPay(money, customer, description);

            return articleService.convertToArticleResponse(article);
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerError("Đăng bài thất bại");
        }
    }

    @Transactional
    @Override
    public ArticleResponse updateArticle(UserDetails currentUser, Integer id, ArticleUpdateRequest articleUpdateRequest)
            throws Exception {
//        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
//        if (customer == null)
//            throw new NotFoundException("Không tìm thấy tài khoản");
//        else if (!customer.getEnabled())
//            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
//        else if (customer.getDeleted())
//            throw new AccessDeniedException("Tài khoản này đang bị khoá");

        try {
            Article article = articleRepository.findByArticleId(id);
            if (article == null)
                throw new NotFoundException("Không tìm thấy bài đăng");
            if (!currentUser.getUsername().equals(article.getCustomer().getEmail()))
                throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài viết này");

            BeanUtils.copyProperties(articleUpdateRequest, article);

            RoomService roomService = article.getRoomService();
            BeanUtils.copyProperties(articleUpdateRequest, roomService);
            article.setRoomService(roomService);

            RoommateRequest roommateRequest = articleUpdateRequest.getRoommateRequest();
            if (roommateRequest != null) {
                Roommate roommate = article.getRoommate() != null ? article.getRoommate() : new Roommate();
                BeanUtils.copyProperties(roommateRequest, roommate);
                article.setRoommate(roommate);
            } else
                article.setRoommate(null);

            Optional<Ward> wardOptional = wardRepository.findById(articleUpdateRequest.getWardId());
            if (!wardOptional.isPresent())
                throw new BadRequestException("Mã phường/xã không hợp lệ");
            article.setWard(wardOptional.get());

            article.setTimeUpdated(new Date());
            article.setDeleted(null);

            article = articleRepository.save(article);

            return articleService.convertToArticleResponse(article);
        } catch (Exception e) {
            throw new InternalServerError("Cập nhật bài viết thất bại");
        }
    }

    @Override
    public MessageResponse deleteArticle(UserDetails currentUser, Integer id) throws BadRequestException {
//        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
//        if (customer == null)
//            throw new NotFoundException("Không tìm thấy tài khoản");
//        else if (!customer.getEnabled())
//            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
//        else if (customer.getDeleted())
//            throw new AccessDeniedException("Tài khoản này đang bị khoá");

        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");

        if (!currentUser.getUsername().equals(article.getCustomer().getEmail()))
            throw new AccessDeniedException("Bạn không có quyền xoá bài viết này");
        articleRepository.delete(article);

        return new MessageResponse("Xóa bài đăng thành công");
    }

    @Override
    public MessageResponse extensionExp(UserDetails currentUser, Integer id, ExtendArticleExpRequest extendArticleExpRequest) throws Exception {
//        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
//        if (customer == null)
//            throw new NotFoundException("Không tìm thấy tài khoản");
//        else if (!customer.getEnabled())
//            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
//        else if (customer.getDeleted())
//            throw new AccessDeniedException("Tài khoản này đang bị khoá");

        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");
        else if (article.getDeleted())
            throw new ConflictException("Gia hạn chỉ áp dụng với bài đăng đã được duyệt");

        if (!currentUser.getUsername().equals(article.getCustomer().getEmail()))
            throw new AccessDeniedException("Bạn không có quyền gia hạn thời gian cho bài viết này");

        //kiểm tra và trừ tiền
        Integer money = 0, days = 0;
        int priceDay = article.getVip() ? 10000 : 2000;
        int priceWeek = article.getVip() ? 60000 : 12000;
        int priceMonth = article.getVip() ? 200000 : 40000;
        String timeType = extendArticleExpRequest.getTimeType();
        Integer times = extendArticleExpRequest.getTimes();
        switch (timeType) {
            case "day":
                days = times;
                money = times * priceDay;
                break;
            case "week":
                days = helper.calculateDays(times, timeType, new Date());
                money = times * priceWeek;
                break;
            case "month":
                days = helper.calculateDays(times, timeType, new Date());
                money = times * priceMonth;
                break;
            default:
                throw new BadRequestException("Loại thời gian không hợp lệ");
        }

        Customer customer = article.getCustomer();
        if (customer.getAccountBalance() < money)
            throw new BadRequestException("Số tiền trong tài khoản không đủ");
        customer.setAccountBalance(customer.getAccountBalance() - money);

        //tạo thời hạn
        article.setExpTime(helper.addDayForDate(days, article.getExpTime()));

        customer = customerRepository.save(customer);

        articleRepository.save(article);

        String description = "Thanh toán " + money + " VNĐ cho bài đăng số: " + article.getArticleId() + " - " + article.getTitle();
        createTransactionPay(money, customer, description);

        return new MessageResponse("Gia hạn bài đăng số: " + id + " thành công");
    }

    @Override
    public MessageResponse hideArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");
        else if (article.getDeleted())
            throw new ConflictException("Bài đăng đã ẩn");

        if (!currentUser.getUsername().equals(article.getCustomer().getEmail()))
            throw new AccessDeniedException("Bạn không có quyền ẩn bài viết này");

        article.setDeleted(true);
        articleRepository.save(article);

        return new MessageResponse("Ẩn bài đăng số: " + id + " thành công");
    }

    @Override
    public MessageResponse showArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null) {
            throw new NotFoundException("Không tìm thấy bài đăng");
        } else {
            if (article.getDeleted() == null)
                throw new AccessDeniedException("Bài đăng số: " + id + " chưa được duyệt");
            else if (!article.getDeleted())
                throw new ConflictException("Bài đăng số: " + id + " không bị ẩn");
            else if (article.getExpTime().after(new Date()))
                throw new AccessDeniedException("Bài đăng số: " + id + " đã hết hạn");
        }

        if (!currentUser.getUsername().equals(article.getCustomer().getEmail()))
            throw new AccessDeniedException("Bạn không có quyền hiển thị bài viết này");

        article.setDeleted(false);

        articleRepository.save(article);
        return new MessageResponse("Hiển thị lại bài đăng đã ẩn số: " + id + " thành công");
    }

    private void createTransactionPay(Integer amount, Customer customer, String description) {
        Transaction transaction = new Transaction();
        transaction.setType(false);
        transaction.setAmount(amount);
        transaction.setCustomer(customer);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
}
