package com.phamthehuy.doan.controller.common;

import com.phamthehuy.doan.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
public class NewsController {
    @Autowired
    private NewsService newsService;

    @GetMapping("/suggestion")
    public ResponseEntity<?> getListNews(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                         @RequestParam(value = "limit", defaultValue = "6") Integer limit) {
        return new ResponseEntity<>(newsService.listNews(page, limit), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> getListNewsNotHidden() {
        return new ResponseEntity<>(newsService.listNewsNotHidden(), HttpStatus.OK);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getNewsBySlug(@PathVariable String slug) throws Exception {
        return new ResponseEntity<>(newsService.getNewsBySlug(slug), HttpStatus.OK);
    }
}
