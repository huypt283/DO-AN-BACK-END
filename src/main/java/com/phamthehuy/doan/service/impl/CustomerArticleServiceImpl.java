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
import com.phamthehuy.doan.service.FavoriteArticleService;
import com.phamthehuy.doan.util.SlugUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerArticleServiceImpl implements CustomerArticleService {
    @Autowired
    private ArticleServiceImpl articleService;
    @Autowired
    private FavoriteArticleService favoriteArticleService;
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

    @Value("${price.dayVip}")
    private Integer dayVip;
    @Value("${price.dayNotVip}")
    private Integer dayNotVip;
    @Value("${price.weekVip}")
    private Integer weekVip;
    @Value("${price.weekNotVip}")
    private Integer weekNotVip;
    @Value("${price.monthVip}")
    private Integer monthVip;
    @Value("${price.monthNotVip}")
    private Integer monthNotVip;

    @Override
    public List<ArticleResponse> listArticleNotHidden(String roomType, String title,
                                                      Integer ward, Integer district, Integer city,
                                                      Integer minPrice, Integer maxPrice,
                                                      Integer minAcreage, Integer maxAcreage, String email) throws Exception {
        List<Article> articles = articleRepository.findCustomNotHidden(roomType, title, ward, district, city, minPrice, maxPrice, minAcreage, maxAcreage);
        final List<FavoriteArticle> favoriteArticles = favoriteArticleService.listFavoriteArticle(email);
        return articles.stream().map(article -> articleService.convertToArticleResponseWithFavorite(article, favoriteArticles)).collect(Collectors.toList());
    }

    @Override
    public ArticleResponse getArticleBySlug(String slug, String email) throws Exception {
        Article article = articleRepository.findBySlug(slug);
        if (article == null) {
            throw new NotFoundException("B??i vi???t kh??ng t???n t???i");
        }
        if (article.getDeleted() == null || article.getDeleted() || article.getBlocked()) {
            throw new NotFoundException("B??i vi???t b??? ???n ho???c ch??a ???????c duy???t");
        }
        final List<FavoriteArticle> favoriteArticles = favoriteArticleService.listFavoriteArticle(email);
        return articleService.convertToArticleResponseWithFavorite(article, favoriteArticles);
    }

    @Override
    public List<ArticleResponse> getListSuggestionArticle(String email, Integer page, Integer limit) throws Exception {
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest((page - 1) * limit, limit, Sort.by("timeUpdated").descending().and(Sort.by("timeCreated").descending()));
        List<Article> articles = new ArrayList<>();
        if (!email.equals("") && email.contains("@")) {
            Customer customer = customerRepository.findByEmail(email);
            if (customer != null) {
                Set<FavoriteArticle> favoriteArticles = customer.getFavoriteArticles();
                if (favoriteArticles != null && favoriteArticles.size() > 0) {
                    articles = articleRepository.suggestion(favoriteArticles);
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
            throw new NotFoundException("Kh??ng t??m th???y b??i ????ng");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("B???n kh??ng c?? quy???n xem chi ti???t b??i vi???t n??y");

        return articleService.convertToArticleResponse(article);
    }

    @Transactional
    @Override
    public MessageResponse insertArticle(UserDetails currentUser, ArticleInsertRequest articleInsertRequest)
            throws Exception {
        Optional<Ward> ward = wardRepository.findById(articleInsertRequest.getWardId());
        if (!ward.isPresent())
            throw new BadRequestException("M?? ph?????ng/x?? kh??ng h???p l???");

        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
        if (customer == null)
            throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        else
            validateCustomer(customer);

        //ki???m tra v?? tr??? ti???n
        Integer money = 0;
        int priceDay = articleInsertRequest.getVip() ? dayVip : dayNotVip;
        int priceWeek = articleInsertRequest.getVip() ? weekVip : weekNotVip;
        int priceMonth = articleInsertRequest.getVip() ? monthVip : monthNotVip;

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
                throw new BadRequestException("Lo???i th???i gian kh??ng h???p l???");
        }
        if (customer.getAccountBalance() < money)
            throw new ConflictException("S??? d?? trong t??i kho???n kh??ng ?????");

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

        String description = "Thanh to??n " + money + " VN?? cho b??i ????ng s???: " + article.getArticleId() + " - " + article.getTitle();
        createTransactionPay(money, customer, description);

        return new MessageResponse("????ng b??i th??nh c??ng");
    }

    @Transactional
    @Override
    public MessageResponse updateArticle(UserDetails currentUser, Integer id, ArticleUpdateRequest articleUpdateRequest)
            throws Exception {
        Optional<Ward> wardOptional = wardRepository.findById(articleUpdateRequest.getWardId());
        if (!wardOptional.isPresent())
            throw new BadRequestException("M?? ph?????ng/x?? kh??ng h???p l???");

        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Kh??ng t??m th???y b??i ????ng");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("B???n kh??ng c?? quy???n ch???nh s???a b??i vi???t n??y");

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

        return new MessageResponse("C???p nh???t b??i ????ng th??nh c??ng");
    }

    @Override
    public MessageResponse deleteArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Kh??ng t??m th???y b??i ????ng");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("B???n kh??ng c?? quy???n xo?? b??i vi???t n??y");

        articleRepository.delete(article);

        return new MessageResponse("X??a b??i ????ng th??nh c??ng");
    }

    @Override
    public MessageResponse extendExp(UserDetails currentUser, Integer id, ExtendArticleExpRequest extendArticleExpRequest) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Kh??ng t??m th???y b??i ????ng");
        else if (BooleanUtils.isTrue(article.getBlocked()))
            throw new ConflictException("Gia h???n kh??ng ??p d???ng v???i b??i ????ng ???? b??? kho??");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("B???n kh??ng c?? quy???n gia h???n th???i gian cho b??i vi???t n??y");

        //ki???m tra v?? tr??? ti???n
        Integer money = 0, days = 0;
        int priceDay = article.getVip() ? dayVip : dayNotVip;
        int priceWeek = article.getVip() ? weekVip : weekNotVip;
        int priceMonth = article.getVip() ? monthVip : monthNotVip;
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
                throw new BadRequestException("Lo???i th???i gian kh??ng h???p l???");
        }

        if (customer.getAccountBalance() < money)
            throw new ConflictException("S??? ti???n trong t??i kho???n kh??ng ?????");

        if (article.getDeleted() == null) {
            //?????t ng??y ????? duy???t
            article.setDays(article.getDays() + days);
        } else {
            //t???o th???i h???n
            article.setExpTime(helper.addDayForDate(days, article.getExpTime()));

            if (article.getDeleted())
                article.setDeleted(false);
        }

        customer.setAccountBalance(customer.getAccountBalance() - money);
        customer = customerRepository.save(customer);

        articleRepository.save(article);

        String description = "Thanh to??n " + money + " VN?? cho b??i ????ng s???: " + article.getArticleId() + " - " + article.getTitle();
        createTransactionPay(money, customer, description);

        return new MessageResponse("Gia h???n b??i ????ng th??nh c??ng");
    }

    @Override
    public MessageResponse hideArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("Kh??ng t??m th???y b??i ????ng");
        else if (article.getDeleted() == null)
            throw new ConflictException("B??i ????ng ch??a ???????c duy???t");
        else if (BooleanUtils.isTrue(article.getDeleted()))
            throw new ConflictException("B??i ????ng ???? ???n");

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("B???n kh??ng c?? quy???n ???n b??i vi???t n??y");

        article.setDeleted(true);
        articleRepository.save(article);

        return new MessageResponse("???n b??i ????ng th??nh c??ng");
    }

    @Override
    public MessageResponse showArticle(UserDetails currentUser, Integer id) throws BadRequestException {
        Article article = articleRepository.findByArticleId(id);
        if (article == null) {
            throw new NotFoundException("Kh??ng t??m th???y b??i ????ng");
        } else {
            if (article.getDeleted() == null)
                throw new ConflictException("B??i ????ng ch??a ???????c duy???t");
            else if (BooleanUtils.isTrue(article.getBlocked()))
                throw new ConflictException("B??i ????ng ??ang b??? kho??");
            else if (BooleanUtils.isFalse(article.getDeleted()))
                throw new ConflictException("B??i ????ng kh??ng b??? ???n");
            else if (new Date().after(article.getExpTime()))
                throw new ConflictException("B??i ????ng ???? h???t h???n");
        }

        Customer customer = article.getCustomer();
        validateCustomer(customer);

        if (!currentUser.getUsername().equals(customer.getEmail()))
            throw new AccessDeniedException("B???n kh??ng c?? quy???n hi???n th??? b??i vi???t n??y");

        article.setDeleted(false);

        articleRepository.save(article);
        return new MessageResponse("Hi???n th??? l???i b??i ????ng th??nh c??ng");
    }

    private void createTransactionPay(Integer amount, Customer customer, String description) {
        Transaction transaction = new Transaction();
        transaction.setPayment(false);
        transaction.setStatus("Th??nh c??ng");
        transaction.setAmount(amount);
        transaction.setCustomer(customer);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new AccessDeniedException("Kh??ng t??m th???y t??i kho???n ????ng b??i");
        } else if (BooleanUtils.isNotTrue(customer.getEnabled()))
            throw new AccessDeniedException("T??i kho???n n??y ch??a ???????c k??ch ho???t");
        else if (BooleanUtils.isTrue(customer.getDeleted()))
            throw new AccessDeniedException("T??i kho???n n??y ???? b??? kho??");
    }
}
