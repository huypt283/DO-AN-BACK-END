package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.service.AdminArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/articles")
public class AdminArticleController {
    @Autowired
    private AdminArticleService adminArticleService;

    @GetMapping
    public ResponseEntity<?> listAllArticle() {
        List<ArticleResponse> articleResponses = adminArticleService.listAllArticle();
        return new ResponseEntity<>(articleResponses, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detailArticle(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(adminArticleService.detailArticle(id), HttpStatus.OK);
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

    @PostMapping("/contact/{id}")
    public ResponseEntity<?> contactToCustomer(@PathVariable Integer id,
                                               @Valid @RequestBody ContactCustomerRequest contactCustomerRequest,
                                               @AuthenticationPrincipal UserDetails admin) throws Exception {
        return new ResponseEntity<>(adminArticleService.contactToCustomer(id, admin, contactCustomerRequest), HttpStatus.OK);
    }

    @PutMapping("/active/{id}")
    public ResponseEntity<?> activeArticle(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserDetails admin) throws Exception {
        return new ResponseEntity<>(adminArticleService.activeArticle(id, admin), HttpStatus.OK);
    }

    @PutMapping("/hide/{id}")
    public ResponseEntity<?> hiddenArticle(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserDetails admin,
                                           @RequestBody String reason) throws Exception {
        return new ResponseEntity<>(adminArticleService.hideArticle(id, admin, reason), HttpStatus.OK);
    }
}
