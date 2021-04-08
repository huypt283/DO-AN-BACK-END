package com.phamthehuy.doan.controller.customer;

import com.phamthehuy.doan.exception.BadRequestException;
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
@RequestMapping("/customer/articles")
public class CustomerArticleController {
    final
    CustomerArticleService customerArticleService;

    public CustomerArticleController(CustomerArticleService customerArticleService) {
        this.customerArticleService = customerArticleService;
    }

    //    list bài đăng cá nhân	/customer/article
    //    lọc bài đăng theo trạng thái: chưa duyệt, đang đăng, đã ẩn	/articles?status={uncheck/active/hidden}
    //    xếp bài đăng theo thời gian (updateTime) tăng/giảm dần	/articles?sort={asc/desc}
    //    lọc bài đang theo loại: thuê phòng/ ở ghép	/articless?roomate={true/false}
    //    lọc bài đăng theo thành phố / huyện / phường	/articles?city= hoặc district= hoặc ward=
    //    lọc bài đăng theo khoảng thời gian (updateTime)	/articles?start={millisecond}&end={millisecond}
    //    lọc bài đăng theo isVip	/articles?vip={true/false}
    //    tìm kiếm bài đăng theo title	/articles?title={title}
    @GetMapping
    public List<ArticleResponse> listMyArticle(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long start,
            @RequestParam(required = false) Long end,
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
        return customerArticleService.listArticleByEmail(email, sort, start, end, ward, district, city,
                roommate, status, vip, search, minAcreage, maxAcreage, page, limit);
    }

    //    chi tiết bài đăng
    @GetMapping("/{id}")
    public ArticleResponse detailArticle(@PathVariable Integer id,
                                         HttpServletRequest request) throws BadRequestException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.detailArticle(email, id);
    }

    //    đăng bài
    @PostMapping
    public ArticleResponse insertArticle(@Valid @RequestBody ArticleInsertRequest articleInsertRequest,
                                         HttpServletRequest request) throws BadRequestException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.insertArticle(email, articleInsertRequest);
    }

    //    sửa bài đăng
    @PutMapping("/{id}")
    public ArticleResponse updateArticle(@Valid @RequestBody ArticleUpdateRequest articleUpdateRequest,
                                         @PathVariable Integer id,
                                         HttpServletRequest request) throws BadRequestException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.updateArticle(email, articleUpdateRequest, id);
    }



    //    xóa bài đăng	/customer/article/{id}
    @DeleteMapping("/{id}")
    public MessageResponse deleteArticle(@PathVariable Integer id,
                                         HttpServletRequest request) throws BadRequestException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.deleteArticle(email, id);
    }

    //    gia hạn bài đăng	/customer/article/extension/{id}?days={int}
    @GetMapping("/extend/{id}")
    public MessageResponse extendExp(@PathVariable Integer id,
                                        @RequestParam Integer number,
                                        @RequestParam String type,
                                        HttpServletRequest request) throws BadRequestException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.extensionExp(email, id, number, type);
    }

    //    ẩn bài đăng
    @PutMapping("/hide/{id}")
    public MessageResponse hideArticle(@PathVariable Integer id,
                                       HttpServletRequest request) throws BadRequestException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.hideArticle(email, id);
    }

    //    đăng lại bài đăng đã ẩn	/customer/article/post/{id}?days={int}
    @PutMapping("/post/{id}")
    public MessageResponse showArticle(@PathVariable Integer id,
                                          HttpServletRequest request) throws BadRequestException {
        String email = (String) request.getAttribute("email");
        return customerArticleService.showArticle(email, id);
    }
}
