package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.entity.StaffArticle;
import com.phamthehuy.doan.exception.AccessDeniedException;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.exception.ConflictException;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.BlockArticleRequest;
import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.ArticleRepository;
import com.phamthehuy.doan.repository.StaffArticleRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.AdminArticleService;
import com.phamthehuy.doan.util.MailSender;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminArticleServiceImpI implements AdminArticleService {
    @Autowired
    private ArticleServiceImpl articleService;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private StaffArticleRepository staffArticleRepository;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private Helper helper;

    @Value("${client.url}")
    private String clientUrl;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    public List<ArticleResponse> listAllArticle() {
        List<Article> articles = articleRepository.findAll();
        return articles.stream().map(articleService::convertToArticleResponse).collect(Collectors.toList());
    }

//    @Override
//    public List<ArticleResponse> listArticle(String sort, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer page, Integer limit) {
//        List<Article> articles = articleRepository.findCustom(sort, start, end, ward, district, city,
//                roommate, status, vip, search, minAcreage, maxAcreage);
//
//        return articles.stream().map(articleService::convertToArticleResponse).collect(Collectors.toList());
//    }

    @Override
    public ArticleResponse detailArticle(Integer id) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article == null)
            throw new NotFoundException("B??i ????ng kh??ng t???n t???i");

        return articleService.convertToArticleResponse(article);
    }

    @Override
    public MessageResponse contactToCustomer(Integer id, UserDetails admin,
                                             ContactCustomerRequest contactCustomerRequest) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article != null) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            validateStaff(staff);

            String to = article.getCustomer().getEmail();
            String note = "Nh??n vi??n li??n h???: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Th???i gian: " + simpleDateFormat.format(new Date());

            mailSender.send(to, contactCustomerRequest.getTitle(), contactCustomerRequest.getContent(), note);

            return new MessageResponse("G???i mail th??nh c??ng");
        } else
            throw new NotFoundException("B??i ????ng kh??ng t???n t???i");
    }

    @Override
    public MessageResponse activeArticle(Integer id, UserDetails admin) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article != null) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            validateStaff(staff);

            //duy???t b??i ????ng
            //chuy???n deleted th??nh false
            if (BooleanUtils.isFalse(article.getDeleted()) || BooleanUtils.isTrue(article.getBlocked()))
                throw new ConflictException("B??i ????ng ??ang hi???n th???");
            else if (article.getDays() <= 0 && new Date().after(article.getExpTime())) {
                throw new ConflictException("B??i ????ng c???n ???????c gia h???n");
            }
            article.setDeleted(false);

            //t???o th???i h???n
            if (article.getDays() > 0) {
                article.setExpTime(helper.addDayForDate(article.getDays(), article.getExpTime()));
                article.setDays(0);
            }

            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Duy???t b??i");
            //l??u v???t ng?????i duy???t b??i ????ng
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);

            //g???i th??
            String to = article.getCustomer().getEmail();
            String note = "Nh??n vi??n duy???t b??i: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Th???i gian: " + simpleDateFormat.format(new Date());
            String title = "B??i ????ng s???: " + article.getArticleId() + " ???? ???????c duy???t";
            String content = "<p>B??i ????ng s???: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Ti??u ?????: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Ng?????i ????ng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>S??T: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Th???i gian ????ng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "<p>Th???i gian duy???t b??i: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Tr???ng th??i: <strong><span style=\"color:#2980b9\">???? ???????c duy???t</span></strong></p>\n" +
                    "\n" +
                    "<p>Th???i gian h???t h???n (?????c t??nh): <span style=\"color:#c0392b\">" + simpleDateFormat.format(article.getExpTime()) + "</span></p>\n" +
                    "\n" +
                    "<p>B??i ????ng c???a b???n ???? ???????c nh??n vi??n <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) duy???t v??o l??c <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>B???n c?? th??? v??o theo ???????ng d???n sau ????? xem b??i vi???t c???a m??nh:</p>\n" +
                    "\n" +
                    "<p> " + this.clientUrl + "/bai-dang/" + article.getSlug() + " </p>\n";

            mailSender.send(to, title, content, note);

            return new MessageResponse("Duy???t b??i th??nh c??ng");
        } else
            throw new NotFoundException("B??i ????ng kh??ng t???n t???i");
    }

    @Override
    public MessageResponse unblockArticle(Integer id, UserDetails admin) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article != null) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            validateStaff(staff);

            //m??? kho?? b??i ????ng
            //chuy???n blocked th??nh false
            if (BooleanUtils.isFalse(article.getBlocked()))
                throw new ConflictException("B??i ????ng kh??ng b??? kho??");
            article.setBlocked(false);

            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("M??? kho?? b??i");
            //l??u v???t ng?????i m??? kho?? cho b??i ????ng
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);

            //g???i th??
            String to = article.getCustomer().getEmail();
            String note = "Nh??n vi??n m??? kho?? b??i ????ng: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Th???i gian: " + simpleDateFormat.format(new Date());
            String title = "B??i ????ng s???: " + article.getArticleId() + " ???? ???????c m??? kho??";
            String content = "<p>B??i ????ng s???: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Ti??u ?????: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Ng?????i ????ng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>S??T: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Th???i gian ????ng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "<p>Th???i gian m??? kho?? b??i ????ng: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>B??i ????ng c???a b???n ???? ???????c nh??n vi??n <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) m??? kho?? v??o l??c <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n";

            mailSender.send(to, title, content, note);

            return new MessageResponse("M??? kho?? b??i ????ng th??nh c??ng");
        } else
            throw new NotFoundException("B??i ????ng kh??ng t???n t???i");
    }

    @Override
    public MessageResponse blockArticle(Integer id, UserDetails admin, BlockArticleRequest blockArticleRequest) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article != null) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            validateStaff(staff);

            //???n b??i ????ng
            //chuy???n deleted th??nh true
            if (BooleanUtils.isTrue(article.getBlocked()))
                throw new ConflictException("B??i ????ng ??ang b??? kho??");

            article.setBlocked(true);

            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Kho?? b??i");
            //l??u v???t ng?????i ???n b??i ????ng
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);
            String reason = blockArticleRequest.getReason();
            //g???i th??
            String to = article.getCustomer().getEmail();
            String note = "Nh??n vi??n kho?? b??i ????ng: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Th???i gian: " + simpleDateFormat.format(new Date());
            String title = "B??i ????ng s???: " + article.getArticleId() + " ???? b??? kho??";
            String content = "<p>B??i ????ng s???: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Ti??u ?????: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Ng?????i ????ng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>S??T: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Th???i gian ????ng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "<p>Th???i gian kho?? b??i ????ng: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>L?? do: <strong><span style=\"color:blue\">" + reason + "</span></strong></p>\n" +
                    "\n" +
                    "<p>B??i ????ng c???a b???n ???? b??? nh??n vi??n <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) kho?? v??o l??c <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>Ch??ng t??i r???t ti???c v??? ??i???u n??y, b???n vui l??ng xem l???i b??i ????ng c???a m??nh ???? ph?? h???p v???i n???i quy website ch??a. H??y c???p nh???t b??i ????ng theo ????ng n???i quy ????? ???????c duy???t l???i. M???i th???c m???c xin li??n h??? theo email nh??n vi??n ???? duy???t b??i.</p>\n";

            mailSender.send(to, title, content, note);

            return new MessageResponse("Kho?? b??i ????ng th??nh c??ng");
        } else
            throw new NotFoundException("B??i ????ng kh??ng t???n t???i");
    }

    public void validateStaff(Staff staff) {
        if (staff == null)
            throw new AccessDeniedException("Kh??ng t??m th???y t??i kho???n");
        if (BooleanUtils.isNotTrue(staff.getEnabled()))
            throw new AccessDeniedException("T??i kho???n n??y ch??a ???????c k??ch ho???t");
        else if (BooleanUtils.isTrue(staff.getDeleted()))
            throw new AccessDeniedException("T??i kho???n n??y ???? b??? kho??");
    }
}
