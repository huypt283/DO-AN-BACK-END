package com.phamthehuy.doan.controller.customer;

import com.phamthehuy.doan.model.enums.RoomType;
import com.phamthehuy.doan.model.request.ArticleInsertRequest;
import com.phamthehuy.doan.model.request.ArticleUpdateRequest;
import com.phamthehuy.doan.model.request.ExtendArticleExpRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.service.CustomerArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/customer/articles")
public class CustomerArticleController {
    @Autowired
    private CustomerArticleService customerArticleService;

    //    list bài đăng cá nhân
    //    lọc bài đăng theo trạng thái
    //    lọc bài đang theo loại phòng
    //    lọc bài đăng theo khoảng thời gian	?start={millisecond}&end={millisecond}
    //    lọc bài đăng theo isVip
    //    tìm kiếm bài đăng theo title
    @GetMapping
    public List<ArticleResponse> listArticle(@RequestParam(required = false) Long start,
                                             @RequestParam(required = false) Long end,
                                             @RequestParam(required = false) RoomType roomType,
                                             @RequestParam(required = false) Integer ward,
                                             @RequestParam(required = false) Integer district,
                                             @RequestParam(required = false) Integer city,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) Boolean vip,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(required = false) Integer minAcreage,
                                             @RequestParam(required = false) Integer maxAcreage,
                                             @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return customerArticleService.listCurrentUserArticleByEmail(start, end, roomType != null ? roomType.toString() : null, ward, district, city,
                status, vip, title, minAcreage, maxAcreage, currentUser);
    }

    //    chi tiết bài đăng
    @GetMapping("/{id}")
    public ResponseEntity<?> detailArticle(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(customerArticleService.detailArticle(currentUser, id), HttpStatus.OK);
    }

    //    đăng bài
    @PostMapping
    public ResponseEntity<?> insertArticle(@Valid @RequestBody ArticleInsertRequest articleInsertRequest,
                                           @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(customerArticleService.insertArticle(currentUser, articleInsertRequest), HttpStatus.OK);
    }

    //    sửa bài đăng
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Integer id,
                                           @Valid @RequestBody ArticleUpdateRequest articleUpdateRequest,
                                           @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(customerArticleService.updateArticle(currentUser, id, articleUpdateRequest), HttpStatus.OK);
    }

    //    xóa bài đăng
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(customerArticleService.deleteArticle(currentUser, id), HttpStatus.OK);
    }

    //    gia hạn bài đăng
    @PostMapping("/extend/{id}")
    public ResponseEntity<?> extendExp(@PathVariable Integer id,
                                       @Valid @RequestBody ExtendArticleExpRequest extendArticleExpRequest,
                                       @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(customerArticleService.extendExp(currentUser, id, extendArticleExpRequest), HttpStatus.OK);
    }

    //    ẩn bài đăng
    @PostMapping("/hide/{id}")
    public ResponseEntity<?> hideArticle(@PathVariable Integer id,
                                         @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(customerArticleService.hideArticle(currentUser, id), HttpStatus.OK);
    }

    //    hiển thị lại bài đăng đã ẩn
    @PostMapping("/show/{id}")
    public ResponseEntity<?> showArticle(@PathVariable Integer id,
                                         @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(customerArticleService.showArticle(currentUser, id), HttpStatus.OK);
    }
}
