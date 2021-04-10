package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.entity.StaffArticle;
import com.phamthehuy.doan.exception.AccessDeniedException;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.exception.ConflictException;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.ArticleRepository;
import com.phamthehuy.doan.repository.StaffArticleRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.AdminArticleService;
import com.phamthehuy.doan.util.MailSender;
import com.phamthehuy.doan.util.auth.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    private JwtUtil jwtUtil;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private Helper helper;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public List<ArticleResponse> listAllArticle() {
        List<Article> articles = articleRepository.findAll();
        return articles.stream().map(articleService::convertToOutputDTO).collect(Collectors.toList());
    }

    @Override
    public List<ArticleResponse> listArticle(String sort, Long start, Long end, Integer ward, Integer district, Integer city, Boolean roommate, String status, Boolean vip, String search, Integer minAcreage, Integer maxAcreage, Integer page, Integer limit) {
        List<Article> articles = articleRepository.findCustom(sort, start, end, ward, district, city,
                        roommate, status, vip, search, minAcreage, maxAcreage);

        return articles.stream().map(articleService::convertToOutputDTO).collect(Collectors.toList());
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    public MessageResponse contactToCustomer(Integer id,
                                             ContactCustomerRequest contactCustomerRequest,
                                             HttpServletRequest request) throws BadRequestException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Staff staff;
            try {
                staff = findStaffByJWT(request);
            } catch (ExpiredJwtException e) {
                throw new BadRequestException("JWT hết hạn");
            } catch (Exception e) {
                throw new BadRequestException("JWT không hợp lệ");
            }


            String to = articleOptional.get().getCustomer().getEmail();

            String note = "Nhân viên liên hệ: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());

            mailSender.send(to, contactCustomerRequest.getTitle(), contactCustomerRequest.getContent(), note);
            return new MessageResponse("Gửi mail thành công");
        } else throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");
    }

    @Override
    public MessageResponse activeArticle(Integer id, UserDetails admin) throws Exception {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Staff staff = staffRepository.findByEmail(admin.getUsername());
            if (staff == null)
                throw new NotFoundException("Không tìm thấy tài khoản");
            else if (!staff.getEnabled())
                throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
            else if (staff.getDeleted())
                throw new AccessDeniedException("Tài khoản này đang bị khoá");

            //duyệt bài
            //chuyển deleted thành false
            Article article = articleOptional.get();
            if (article.getDeleted() != null)
                throw new ConflictException("Chỉ được duyệt bài có trạng thái là chưa duyệt");
            article.setDeleted(false);

            //tạo thời hạn
            article.setExpTime(helper.addDayForDate(article.getDays(), new Date()));
            article.setDays(null);

            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction(true);
            //lưu vết người duyệt bài
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);
            ArticleResponse articleResponse = articleService.convertToOutputDTO(article);

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
                    "<p>Thời gian đăng: " + article.getTimeCreated() + "</p>\n" +
                    "\n" +
                    "\n" +
                    "<p>Thời gian duyệt bài: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Trạng thái: <strong><span style=\"color:#2980b9\">đã được duyệt</span></strong></p>\n" +
                    "\n" +
                    "<p>Thời gian hết hạn (ước tính): <span style=\"color:#c0392b\">" + simpleDateFormat.format(articleResponse.getExpDate()) + "</span></p>\n" +
                    "\n" +
                    "<p>Bài đăng của bạn đã được nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) duyệt vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>Bạn có thể vào theo đường dẫn sau để xem bài viết của mình:</p>\n" +
                    "\n" +
                    "<p> "+ this.clientUrl + "/articles/" + article.getSlug() + " </p>\n";
            mailSender.send(to, title, content, note);
            return new MessageResponse("Duyệt bài thành công");
        } else
            throw new BadRequestException("Bài đăng với không tồn tại");
    }

    @Override
    public MessageResponse hiddenArticle(Integer id, String reason, HttpServletRequest request) throws BadRequestException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Staff staff;
            try {
                staff = findStaffByJWT(request);
            } catch (ExpiredJwtException e) {
                throw new BadRequestException("JWT hết hạn");
            } catch (Exception e) {
                throw new BadRequestException("JWT không hợp lệ");
            }

            //duyệt bài
            //chuyển deleted thành true
            Article article = articleOptional.get();
            if (!article.getDeleted())
                throw new BadRequestException("Không thể ẩn bài viết đang ẩn");

            article.setDeleted(true);

            article.setExpTime(null);

            //tạo bản ghi staffArticle
            StaffArticle staffArticle = new StaffArticle();
            staffArticle.setTime(new Date());
            staffArticle.setStaff(staff);
            staffArticle.setArticle(article);
            staffArticle.setAction(false);
            //lưu
            staffArticleRepository.save(staffArticle);

            article = articleRepository.save(article);
            ArticleResponse articleResponse = articleService.convertToOutputDTO(article);

            //gửi thư
            if (reason == null || reason.trim().equals("")) reason = "không có lý do cụ thể";
            String to = article.getCustomer().getEmail();
            String note = "Nhân viên duyệt bài: " + staff.getName() + "<br/>"
                    + "Email: " + staff.getEmail() + "<br/>"
                    + "Thời gian: " + simpleDateFormat.format(new Date());
            String title = "Bài đăng số: " + article.getArticleId() + " đã bị ẩn";
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
                    "\n" +
                    "<p>Thời gian ẩn bài: " + simpleDateFormat.format(new Date()) + "</p>\n" +
                    "\n" +
                    "<p>Trạng thái: <strong><span style=\"color:red\">đã bị ẩn</span></strong></p>\n" +
                    "\n" +
                    "<p>Lý do: <strong><span style=\"color:blue\">" + reason + "</span></strong></p>\n" +
                    "\n" +
                    "<p>Bài đăng của bạn đã bị nhân viên <em><strong>" + staff.getName() + " </strong></em>(email: <em><strong>" + staff.getEmail() + "</strong></em>) ẩn vào lúc <em><strong>" + simpleDateFormat.format(new Date()) + "</strong></em>.</p>\n" +
                    "\n" +
                    "<p>Chúng tôi rất tiếc về điều này, bạn vui lòng xem lại bài đăng của mình đã phù hợp với nội quy website chưa. Mọi thắc mắc xin liên hệ theo email nhân viên đã duyệt bài.</p>\n";
            mailSender.send(to, title, content, note);
            return new MessageResponse("Ẩn bài thành công");
        } else throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");
    }

    @Override
    public ArticleResponse detailArticle(Integer id) throws BadRequestException {
        Optional<Article> articleOptional = articleRepository.findById(id);
        if (!articleOptional.isPresent())
            throw new BadRequestException("Bài đăng với id: " + id + " không tồn tại");
        Article article = articleOptional.get();
        return articleService.convertToOutputDTO(article);
    }

    private Staff findStaffByJWT(HttpServletRequest request) throws Exception {
        String jwt = extractJwtFromRequest(request);
        if (jwt == null || jwt.trim().equals("")) throw new BadRequestException("Không có JWT");
        String email = jwtUtil.getEmailFromToken(jwt);
        if (email == null || email.trim().equals("")) throw new BadRequestException("JWT không hợp lệ");
        Staff staff = staffRepository.findByEmail(email);
        if (staff == null) throw new BadRequestException("JWT không hợp lệ");
        return staff;
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }

}
