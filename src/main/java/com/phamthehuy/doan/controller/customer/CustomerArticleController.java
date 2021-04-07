package com.phamthehuy.doan.controller.customer;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.ArticleInsertRequest;
import com.phamthehuy.doan.model.request.ArticleUpdateRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.service.CustomerArticleService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerArticleController {
    final
    CustomerArticleService customerArticleService;

    public CustomerArticleController(CustomerArticleService customerArticleService) {
        this.customerArticleService = customerArticleService;
    }

    //    list bài đăng cá nhân	/customer/article
//    lọc bài đăng theo trạng thái: chưa duyệt, đang đăng, đã ẩn	/customer/article?status={uncheck/active/hidden}
//    xếp bài đăng theo thời gian (updateTime) tăng/giảm dần	/admin/article?sort={asc/desc}
//    lọc bài đang theo loại: thuê phòng/ ở ghép	/admin/article?roomate={true/false}
//    lọc bài đăng theo thành phố / huyện / phường	/admin/article?city= hoặc district= hoặc ward=
//    lọc bài đăng theo khoảng thời gian (updateTime)	/admin/article?start={millisecond}&end={millisecond}
//    lọc bài đăng theo isVip	/admin/article?vip={true/false}
//    tìm kiếm bài đăng theo title	/customer/article?title={title}
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
            @RequestParam Integer limit,
            HttpServletRequest request
    ) {
        String email = (String) request.getAttribute("email");
        return customerArticleService.listArticle(email, sort, start, end, ward, district, city,
                roommate, status, vip, search, minAcreage, maxAcreage, page, limit);
    }

    //    chi tiết bài đăng	/customer/article/{id}
    @GetMapping("/article/{id}")
    public ArticleResponse detailArticle(@PathVariable Integer id,
                                         HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.detailArticle(email, id);
    }

    //    đăng bài	/customer/article
    @PostMapping("/article")
    public ArticleResponse insertArticle(@Valid @RequestBody ArticleInsertRequest articleInsertRequest,
                                         HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.insertArticle(email, articleInsertRequest);
    }

    //    sửa bài đăng	/customer/article
    @PutMapping("/article/{id}")
    public ArticleResponse updateArticle(@Valid @RequestBody ArticleUpdateRequest articleUpdateRequest,
                                         @PathVariable Integer id,
                                         HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.updateArticle(email, articleUpdateRequest, id);
    }

    //    ẩn bài đăng	/customer/article/hidden/{id}
    @GetMapping("/article/hidden/{id}")
    public MessageResponse hiddenArticle(@PathVariable Integer id,
                                         HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.hiddenArticle(email, id);
    }

    //    xóa bài đăng	/customer/article/{id}
    @DeleteMapping("/article/{id}")
    public MessageResponse deleteArticle(@PathVariable Integer id,
                                         HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.deleteArticle(email, id);
    }

    //    gia hạn bài đăng	/customer/article/extension/{id}?days={int}
    @GetMapping("/article/extension/{id}")
    public MessageResponse extensionExp(@PathVariable Integer id,
                                        @RequestParam Integer number,
                                        @RequestParam String type,
                                        HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.extensionExp(email, id, number, type);
    }

    //    đăng lại bài đăng đã ẩn	/customer/article/post/{id}?days={int}
    @GetMapping("/article/post/{id}")
    public MessageResponse postOldArticle(@PathVariable Integer id,
                                          @RequestParam Integer number,
                                          @RequestParam String type,
                                          HttpServletRequest request) throws CustomException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.postOldArticle(email, id, number, type);
    }
}
