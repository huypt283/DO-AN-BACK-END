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
            throw new NotFoundException("Bài đăng không tồn tại");

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
            String note = "Nhân viên liên hệ: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());

            mailSender.send(to, contactCustomerRequest.getTitle(), contactCustomerRequest.getContent(), note);

            return new MessageResponse("Gửi mail thành công");
        } else
            throw new BadRequestException("Bài đăng không tồn tại");
    }

    @Override
    public MessageResponse activeArticle(Integer id, UserDetails admin) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article != null) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            validateStaff(staff);

            //duyệt bài đăng
            //chuyển deleted thành false
            if (article.getDeleted() != null || BooleanUtils.isTrue(article.getBlocked()))
                throw new ConflictException("Chỉ được duyệt bài có trạng thái là chưa duyệt");
            else if (article.getDays() <= 0) {
                throw new ConflictException("Bài đăng đã hết hạn");
            }
            article.setDeleted(false);

            //tạo thời hạn
            article.setExpTime(helper.addDayForDate(article.getDays(),
                    article.getExpTime() != null ? article.getExpTime() : new Date()));
            article.setDays(0);

            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Duyệt bài");
            //lưu vết người duyệt bài đăng
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);

            //gửi thư
            String to = article.getCustomer().getEmail();
            String note = "Nhân viên duyệt bài: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());
            String title = "Bài đăng số: " + article.getArticleId() + " đã được duyệt";
            String content = "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Người đăng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>SĐT: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Thời gian đăng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "<p>Thời gian duyệt bài: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Trạng thái: <strong><span style=\"color:#2980b9\">đã được duyệt</span></strong></p>\n" +
                    "\n" +
                    "<p>Thời gian hết hạn (ước tính): <span style=\"color:#c0392b\">" + simpleDateFormat.format(article.getExpTime()) + "</span></p>\n" +
                    "\n" +
                    "<p>Bài đăng của bạn đã được nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) duyệt vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>Bạn có thể vào theo đường dẫn sau để xem bài viết của mình:</p>\n" +
                    "\n" +
                    "<p> " + this.clientUrl + "/bai-dang/" + article.getSlug() + " </p>\n";

            mailSender.send(to, title, content, note);

            return new MessageResponse("Duyệt bài thành công");
        } else
            throw new NotFoundException("Bài đăng không tồn tại");
    }

    @Override
    public MessageResponse unblockArticle(Integer id, UserDetails admin) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article != null) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            validateStaff(staff);

            //mở khoá bài đăng
            //chuyển blocked thành false
            if (BooleanUtils.isFalse(article.getBlocked()))
                throw new ConflictException("Bài đăng không bị khoá");
            article.setBlocked(false);

            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Mở khoá bài");
            //lưu vết người mở khoá cho bài đăng
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);

            //gửi thư
            String to = article.getCustomer().getEmail();
            String note = "Nhân viên mở khoá bài đăng: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());
            String title = "Bài đăng số: " + article.getArticleId() + " đã được mở khoá";
            String content = "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Người đăng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>SĐT: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Thời gian đăng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "<p>Thời gian mở khoá bài đăng: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Bài đăng của bạn đã được nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) mở khoá vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n";

            mailSender.send(to, title, content, note);

            return new MessageResponse("Duyệt bài thành công");
        } else
            throw new NotFoundException("Bài đăng không tồn tại");
    }

    @Override
    public MessageResponse blockArticle(Integer id, UserDetails admin, BlockArticleRequest blockArticleRequest) throws Exception {
        Article article = articleRepository.findByArticleId(id);
        if (article != null) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            if (staff == null)
                throw new NotFoundException("Không tìm thấy tài khoản");
            else if (!staff.getEnabled())
                throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
            else if (staff.getDeleted())
                throw new AccessDeniedException("Tài khoản này đang bị khoá");

            //ẩn bài đăng
            //chuyển deleted thành true
            if (BooleanUtils.isTrue(article.getBlocked()))
                throw new ConflictException("Bài đăng đang bị khoá");

            article.setBlocked(true);

            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction("Khoá bài");
            //lưu vết người ẩn bài đăng
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);
            String reason = blockArticleRequest.getReason();
            //gửi thư
            String to = article.getCustomer().getEmail();
            String note = "Nhân viên khoá bài đăng: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());
            String title = "Bài đăng số: " + article.getArticleId() + " đã bị khoá";
            String content = "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                    "\n" +
                    "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                    "\n" +
                    "<p>Người đăng: " + article.getCustomer().getName() + "</p>\n" +
                    "\n" +
                    "<p>Email: " + article.getCustomer().getEmail() + "</p>\n" +
                    "\n" +
                    "<p>SĐT: " + article.getCustomer().getPhone() + "</p>\n" +
                    "\n" +
                    "<p>Thời gian đăng: " + simpleDateFormat.format(article.getTimeCreated()) + "</p>\n" +
                    "\n" +
                    "<p>Thời gian khoá bài đăng: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Lý do: <strong><span style=\"color:blue\">" + reason + "</span></strong></p>\n" +
                    "\n" +
                    "<p>Bài đăng của bạn đã bị nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) khoá vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>Chúng tôi rất tiếc về điều này, bạn vui lòng xem lại bài đăng của mình đã phù hợp với nội quy website chưa. Mọi thắc mắc xin liên hệ theo email nhân viên đã duyệt bài.</p>\n";

            mailSender.send(to, title, content, note);

            return new MessageResponse("Khoá bài thành công");
        } else
            throw new NotFoundException("Bài đăng không tồn tại");
    }

    public void validateStaff(Staff staff) {
        if (staff == null)
            throw new NotFoundException("Không tìm thấy tài khoản");
        if (BooleanUtils.isNotTrue(staff.getEnabled()))
            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
        else if (BooleanUtils.isTrue(staff.getDeleted()))
            throw new AccessDeniedException("Tài khoản này đã bị khoá");
    }
}
