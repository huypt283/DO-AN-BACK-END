package com.phamthehuy.doan.controller.unauthenticated;

import com.phamthehuy.doan.model.enums.RoomType;
import com.phamthehuy.doan.service.CustomerArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/articles")
public class ArticleController {
    @Autowired
    private CustomerArticleService articleService;

    @GetMapping
    public ResponseEntity<?> getListArticle(@RequestParam(required = false) RoomType roomType,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) Integer ward,
                                            @RequestParam(required = false) Integer district,
                                            @RequestParam(required = false) Integer city,
                                            @RequestParam(required = false) Integer minPrice,
                                            @RequestParam(required = false) Integer maxPrice,
                                            @RequestParam(required = false) Integer minAcreage,
                                            @RequestParam(required = false) Integer maxAcreage) throws Exception {
        return new ResponseEntity<>(articleService.listArticleNotHidden(roomType != null ? roomType.toString() : null, title,
                ward, district, city, minPrice, maxPrice, minAcreage, maxAcreage), HttpStatus.OK);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getArticleBySlug(@PathVariable String slug) throws Exception {
        return new ResponseEntity<>(articleService.getArticleBySlug(slug), HttpStatus.OK);
    }

    @GetMapping("/new")
    public ResponseEntity<?> getListNewArticle(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                               @RequestParam(value = "limit", defaultValue = "6") Integer limit) throws Exception {
        return new ResponseEntity<>(articleService.getListNewArticle(page, limit), HttpStatus.OK);
    }
}
