package com.phamthehuy.doan.controller.unauthenticated;

import com.phamthehuy.doan.service.CustomerArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/articles")
public class ArticleController {
    @Autowired
    private CustomerArticleService articleService;

    @GetMapping
    public ResponseEntity<?> listArticle(@RequestParam(required = false) Long start,
                                         @RequestParam(required = false) Long end,
                                         @RequestParam(required = false) Integer ward,
                                         @RequestParam(required = false) Integer district,
                                         @RequestParam(required = false) Integer city,
                                         @RequestParam(required = false) Boolean roommate,
                                         @RequestParam(required = false) Boolean vip,
                                         @RequestParam(required = false) String search,
                                         @RequestParam(required = false) Integer minAcreage,
                                         @RequestParam(required = false) Integer maxAcreage) throws Exception {
        return new ResponseEntity<>(
                articleService.listArticleNotHidden(start, end, ward, district, city, roommate, vip, search, minAcreage, maxAcreage),
                HttpStatus.OK);
    }
}
