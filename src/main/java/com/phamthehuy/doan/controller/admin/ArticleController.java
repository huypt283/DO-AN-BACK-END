package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.service.ArticleService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/")
public class ArticleController {
    final
    ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    //    list bài đăng	/admin/article
//    xếp bài đăng theo ngày tăng dần	/admin/article?sort=asc
//    xếp bài đăng theo ngày giảm dần	/admin/article?sort=desc
//    lọc bài đăng theo khoảng thời gian	/admin/article?start={millisecond}&end={millisecond}
//    lọc bài đăng theo thành phố / huyện / phường	/admin/article?city= hoặc district= hoặc ward=
//    lọc bài đang theo loại: thuê phòng/ ở ghép	/admin/article?roommate={true/false}
//    lọc bài đăng theo trạng thái: chưa duyệt/ đã duyệt/ đã ẩn	/admin/article?status={uncheck/activated/hidden}
//    lọc bài đăng theo isVip	/admin/article?vip={true/false}
//    tìm kiếm bài đăng theo title/customer-name/customer-phone	/admin/article?search={search}
    @GetMapping("/article")
    public List<ArticleResponse> listArticle(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long start, @RequestParam(required = false) Long end,
            @RequestParam(required = false) Integer ward,
            @RequestParam(required = false) Integer district,
            @RequestParam(required = false) Integer city,
            @RequestParam(required = false) Boolean roommate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean vip,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minAcreage,
            @RequestParam(required = false) Integer maxAcreage,
            @RequestParam Integer page,
            @RequestParam Integer limit
    ) {
        return articleService.listArticle(sort, start, end, ward, district, city,
                roommate, status, vip, search, minAcreage, maxAcreage, page, limit);
    }

    //    contact với khách hàng (gửi mail cho khách hàng về bài viết này)	/admin/article/contact/{id}
    @PostMapping("/article/contact/{id}")
    public MessageResponse contactToCustomer(@PathVariable Integer id,
                                             @Valid @RequestBody ContactCustomerRequest contactCustomerRequest,
                                             HttpServletRequest request) throws CustomException {
        return articleService.contactToCustomer(id, contactCustomerRequest, request);
    }

    //    duyệt bài đăng (hiện) (gửi mail)	/admin/article/active/{id}
    @PostMapping("/article/active/{id}")
    public MessageResponse activeArticle(@PathVariable Integer id, HttpServletRequest request)
            throws CustomException {
        return articleService.activeArticle(id, request);
    }

    //    ẩn bài đăng (gửi mail)	/admin/article/block/{id}
    @PostMapping("/article/hidden/{id}")
    public MessageResponse hiddenArticle(@PathVariable Integer id,
                                         @RequestParam String mess,
                                         HttpServletRequest request)
            throws CustomException {
        return articleService.hiddenArticle(id, mess, request);
    }

    @GetMapping("/article/{id}")
    public ArticleResponse detailArticle(@PathVariable Integer id) throws CustomException {
        return articleService.detailArticle(id);
    }
}
