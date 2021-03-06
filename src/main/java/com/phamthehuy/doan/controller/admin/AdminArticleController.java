package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.request.BlockArticleRequest;
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

    @PostMapping("/contact/{id}")
    public ResponseEntity<?> contactToCustomer(@PathVariable Integer id,
                                               @Valid @RequestBody ContactCustomerRequest contactCustomerRequest,
                                               @AuthenticationPrincipal UserDetails admin) throws Exception {
        return new ResponseEntity<>(adminArticleService.contactToCustomer(id, admin, contactCustomerRequest), HttpStatus.OK);
    }

    @PostMapping("/active/{id}")
    public ResponseEntity<?> activeArticle(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserDetails admin) throws Exception {
        return new ResponseEntity<>(adminArticleService.activeArticle(id, admin), HttpStatus.OK);
    }

    @PostMapping("/unblock/{id}")
    public ResponseEntity<?> showArticle(@PathVariable Integer id,
                                         @AuthenticationPrincipal UserDetails admin) throws Exception {
        return new ResponseEntity<>(adminArticleService.unblockArticle(id, admin), HttpStatus.OK);
    }

    @PostMapping("/block/{id}")
    public ResponseEntity<?> blockArticle(@PathVariable Integer id,
                                          @AuthenticationPrincipal UserDetails admin,
                                          @Valid @RequestBody BlockArticleRequest blockArticleRequest) throws Exception {
        return new ResponseEntity<>(adminArticleService.blockArticle(id, admin, blockArticleRequest), HttpStatus.OK);
    }

    //    list b??i ????ng	/admin/article
    //    x???p b??i ????ng theo ng??y t??ng d???n	/admin/articles?sort=asc
    //    x???p b??i ????ng theo ng??y gi???m d???n	/admin/articles?sort=desc
    //    l???c b??i ????ng theo kho???ng th???i gian	/admin/articles?start={millisecond}&end={millisecond}
    //    l???c b??i ????ng theo th??nh ph??? / huy???n / ph?????ng	/admin/articles?city= ho???c district= ho???c ward=
    //    l???c b??i ??ang theo lo???i: thu?? ph??ng/ ??? gh??p	/admin/articles?roommate={true/false}
    //    l???c b??i ????ng theo tr???ng th??i: ch??a duy???t/ ???? duy???t/ ???? ???n	/admin/articles?status={uncheck/activated/hidden}
    //    l???c b??i ????ng theo isVip	/admin/articles?vip={true/false}
    //    t??m ki???m b??i ????ng theo title/customer-name/customer-phone	/admin/articles?search={search}
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
}
