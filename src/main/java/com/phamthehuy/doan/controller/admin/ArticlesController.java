package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.dao.ArticlesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class ArticlesController {
    final
    ArticlesRepository articlesRepository;

    @Autowired
    public ArticlesController(ArticlesRepository articlesRepository) {
        this.articlesRepository = articlesRepository;
    }

}
