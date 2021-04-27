package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.*;
import com.phamthehuy.doan.exception.AccessDeniedException;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.exception.ConflictException;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.*;
import com.phamthehuy.doan.service.CustomerArticleService;
import com.phamthehuy.doan.util.SlugUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomerArticleServiceImpl implements CustomerArticleService {
    @Autowired
    private ArticleServiceImpl articleService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private RoomateRepository roomateRepository;
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
        if (article.getDeleted() == null || article.getDeleted() || article.getBlocked()) {
            throw new NotFoundException("Bài viết bị ẩn hoặc chưa được duyệt");
        }
        return articleService.convertToArticleResponse(article);
    }

    @Override
    public List<ArticleResponse> getListSuggestionArticle(String email, Integer page, Integer limit) throws Exception {
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest((page - 1) * limit, limit, Sort.by("timeUpdated").descending().and(Sort.by("timeCreated").descending()));
        List<Article> articles = null;
        if (!email.trim().equals("")) {
            Customer customer = customerRepository.findByEmail(email);
            if (customer != null) {
                Set<FavoriteArticle> favoriteArticles = customer.getFavoriteArticles();
                if (favoriteArticles != null && favoriteArticles.size() > 0) {
                    articles = articleRepository.findByWardInAndDeletedFalseAndBlockedFalse(favoriteArticles.stream()
                            .map(favoriteArticle -> favoriteArticle.getArticle().getWard())
                            .collect(Collectors.toSet()), pageable);
                }
            }
        }

        if (articles == null || articles.size() < 1)
            articles = articleRepository.findByDeletedFalseAAndBlockedFalse(pageable);
        else if (articles.size() < 6) {
            pageable = new OffsetBasedPageRequest((page - 1) * limit, 6 - articles.size(), Sort.by("timeUpdated").descending().and(Sort.by("timeCreated").descending()));
            articles.addAll(articleRepository.findByDeletedFalseAAndBlockedFalse(pageable));
        }

        return articles.stream().map(articleService::convertToArticleResponse).collect(Collectors.toList());
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

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("Bạn không có quyền xem chi tiết bài viết này");

        return articleService.convertToArticleResponse(article);
    }

    @Transactional
    @Override
    public ArticleResponse insertArticle(UserDetails currentUser, ArticleInsertRequest articleInsertRequest)
            throws Exception {
        Optional<Ward> ward = wardRepository.findById(articleInsertRequest.getWardId());
        if (!ward.isPresent())
            throw new BadRequestException("Mã phường/xã không hợp lệ");

        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
        if (customer == null)
            throw new NotFoundException("Không tìm thấy tài khoản");
        else
            validateCustomer(customer);

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

        article.setWard(ward.get());

        article.setTimeCreated(new Date());
        article.setDeleted(null);
        article.setBlocked(false);

        customer = customerRepository.save(customer);
        article.setCustomer(customer);

        article = articleRepository.save(article);

        String description = "Thanh toán " + money + " VNĐ cho bài đăng số: " + article.getArticleId() + " - " + article.getTitle();
        createTransactionPay(money, customer, description);

        return articleService.convertToArticleResponse(article);
    }

    @Transactional
    @Override
    public ArticleResponse updateArticle(UserDetails currentUser, Integer id, ArticleUpdateRequest articleUpdateRequest)
            throws Exception {
        Optional<Ward> wardOptional = wardRepository.findById(articleUpdateRequest.getWardId());
        if (!wardOptional.isPresent())
            throw new BadRequestException("Mã phường/xã không hợp lệ");

        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa bài viết này");

        BeanUtils.copyProperties(articleUpdateRequest, article);

        RoomService roomService = article.getRoomService();
        BeanUtils.copyProperties(articleUpdateRequest, roomService);
        article.setRoomService(roomService);
        article.setRoomType(articleUpdateRequest.getRoomType().toString());

        RoommateRequest roommateRequest = articleUpdateRequest.getRoommateRequest();
        if (roommateRequest != null) {
            Roommate roommate = article.getRoommate() != null ? article.getRoommate() : new Roommate();
            BeanUtils.copyProperties(roommateRequest, roommate);
            article.setRoommate(roommate);
        } else {
            Roommate roommate = article.getRoommate();
            if (roommate != null)
                roomateRepository.delete(roommate);
            article.setRoommate(null);
        }

        article.setWard(wardOptional.get());

        article.setTimeUpdated(new Date());
        if (BooleanUtils.isFalse(article.getDeleted()))
            article.setDeleted(null);
        article.setBlocked(false);

        article = articleRepository.save(article);

        return articleService.convertToArticleResponse(article);
    }

    @Override
    public MessageResponse deleteArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("Bạn không có quyền xoá bài viết này");

        articleRepository.delete(article);

        return new MessageResponse("Xóa bài đăng thành công");
    }

    @Override
    public MessageResponse extendExp(UserDetails currentUser, Integer id, ExtendArticleExpRequest extendArticleExpRequest) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");
        else if (BooleanUtils.isTrue(article.getBlocked()))
            throw new ConflictException("Gia hạn không áp dụng với bài đăng đã bị khoá");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
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

        if (customer.getAccountBalance() < money)
            throw new ConflictException("Số tiền trong tài khoản không đủ");

        if (article.getDeleted() == null) {
            //đặt ngày để duyệt
            article.setDays(article.getDays() + days);
        } else {
            //tạo thời hạn
            article.setExpTime(helper.addDayForDate(days, article.getExpTime()));

            if (article.getDeleted())
                article.setDeleted(false);
        }

        customer.setAccountBalance(customer.getAccountBalance() - money);
        customer = customerRepository.save(customer);

        articleRepository.save(article);

        String description = "Thanh toán " + money + " VNĐ cho bài đăng số: " + article.getArticleId() + " - " + article.getTitle();
        createTransactionPay(money, customer, description);

        return new MessageResponse("Gia hạn bài đăng thành công");
    }

    @Override
    public MessageResponse hideArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Không tìm thấy bài đăng");
        else if (article.getDeleted() == null)
            throw new ConflictException("Bài đăng chưa được duyệt");
        else if (BooleanUtils.isTrue(article.getDeleted()))
            throw new ConflictException("Bài đăng đã ẩn");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("Bạn không có quyền ẩn bài viết này");

        article.setDeleted(true);
        articleRepository.save(article);

        return new MessageResponse("Ẩn bài đăng thành công");
    }

    @Override
    public MessageResponse showArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null) {
            throw new NotFoundException("Không tìm thấy bài đăng");
        } else {
            if (article.getDeleted() == null)
                throw new ConflictException("Bài đăng chưa được duyệt");
            else if (BooleanUtils.isTrue(article.getBlocked()))
                throw new ConflictException("Bài đăng đang bị khoá");
            else if (BooleanUtils.isFalse(article.getDeleted()))
                throw new ConflictException("Bài đăng không bị ẩn");
            else if (new Date().after(article.getExpTime()))
                throw new ConflictException("Bài đăng đã hết hạn");
        }

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("Bạn không có quyền hiển thị bài viết này");

        article.setDeleted(false);

        articleRepository.save(article);
        return new MessageResponse("Hiển thị lại bài đăng thành công");
    }

    private void createTransactionPay(Integer amount, Customer customer, String description) {
        Transaction transaction = new Transaction();
        transaction.setPayment(false);
        transaction.setStatus("Thành công");
        transaction.setAmount(amount);
        transaction.setCustomer(customer);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new AccessDeniedException("Không tìm thấy tài khoản đăng bài");
        } else if (BooleanUtils.isNotTrue(customer.getEnabled()))
            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
        else if (BooleanUtils.isTrue(customer.getDeleted()))
            throw new AccessDeniedException("Tài khoản này đã bị khoá");
    }
}
