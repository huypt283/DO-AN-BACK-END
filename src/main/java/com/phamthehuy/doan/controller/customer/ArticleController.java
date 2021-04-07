package com.phamthehuy.doan.controller.customer;

import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.service.CustomerArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {
    @Autowired
    CustomerArticleService customerArticleService;

//    @GetMapping
//    public List<ArticleResponse> listArticleNotHidden(HttpServletRequest request) {
//        String email = (String) request.getAttribute("email");
//        return customerArticleService.listArticleNotHidden();
//    }
}
