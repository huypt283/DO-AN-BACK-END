package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.service.AdminArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/articles")
public class AdminArticleController {
    @Autowired
    private AdminArticleService adminArticleService;

    @GetMapping
    public ResponseEntity<List<ArticleResponse>> listAllArticle() {
        List<ArticleResponse> articleResponses = adminArticleService.listAllArticle();
        return new ResponseEntity<>(articleResponses, HttpStatus.OK);
    }

    //    list bài đăng	/admin/article
    //    xếp bài đăng theo ngày tăng dần	/admin/articles?sort=asc
    //    xếp bài đăng theo ngày giảm dần	/admin/articles?sort=desc
    //    lọc bài đăng theo khoảng thời gian	/admin/articles?start={millisecond}&end={millisecond}
    //    lọc bài đăng theo thành phố / huyện / phường	/admin/articles?city= hoặc district= hoặc ward=
    //    lọc bài đang theo loại: thuê phòng/ ở ghép	/admin/articles?roommate={true/false}
    //    lọc bài đăng theo trạng thái: chưa duyệt/ đã duyệt/ đã ẩn	/admin/articles?status={uncheck/activated/hidden}
    //    lọc bài đăng theo isVip	/admin/articles?vip={true/false}
    //    tìm kiếm bài đăng theo title/customer-name/customer-phone	/admin/articles?search={search}
//    @GetMapping()
//    public List<ArticleResponse> listArticle(
//            @RequestParam(required = false) String sort,
//            @RequestParam(required = false) Long start, @RequestParam(required = false) Long end,
//            @RequestParam(required = false) Integer ward,
//            @RequestParam(required = false) Integer district,
//            @RequestParam(required = false) Integer city,
//            @RequestParam(required = false) Boolean roommate,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) Boolean vip,
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) Integer minAcreage,
//            @RequestParam(required = false) Integer maxAcreage,
//            @RequestParam Integer page,
//            @RequestParam Integer limit
//    ) {
//        return articleService.listArticle(sort, start, end, ward, district, city,
//                roommate, status, vip, search, minAcreage, maxAcreage, page, limit);
//    }

    //    contact với khách hàng (gửi mail cho khách hàng về bài viết này)	/admin/article/contact/{id}
    @PostMapping("/contact/{id}")
    public MessageResponse contactToCustomer(@PathVariable Integer id,
                                             @Valid @RequestBody ContactCustomerRequest contactCustomerRequest,
                                             HttpServletRequest request) throws BadRequestException {
        return adminArticleService.contactToCustomer(id, contactCustomerRequest, request);
    }

    //    duyệt bài đăng (hiện) (gửi mail)	/admin/article/active/{id}
    @PutMapping("/active/{id}")
    public ResponseEntity<MessageResponse> activeArticle(@PathVariable Integer id, HttpServletRequest request)
            throws BadRequestException {
        return new ResponseEntity<>(adminArticleService.activeArticle(id, request), HttpStatus.OK);
    }

    //    ẩn bài đăng (gửi mail)	/admin/article/block/{id}
    @PutMapping("/hide/{id}")
    public MessageResponse hiddenArticle(@PathVariable Integer id,
                                         @RequestParam String mess,
                                         HttpServletRequest request)
            throws BadRequestException {
        return adminArticleService.hiddenArticle(id, mess, request);
    }

    @GetMapping("/{id}")
    public ArticleResponse detailArticle(@PathVariable Integer id) throws BadRequestException {
        return adminArticleService.detailArticle(id);
    }
}
