package com.phamthehuy.doan.controller.customer;

import com.phamthehuy.doan.service.FavoriteArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
public class FavoriteArticleController {
    @Autowired
    private FavoriteArticleService favoriteArticleService;

    @GetMapping("/favorite-article")
    public ResponseEntity<?> listArticle(@AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(favoriteArticleService.listFavoriteArticle(currentUser), HttpStatus.OK);
    }

    @PatchMapping("/favorite-article/{articleId}")
    public ResponseEntity<?> favoriteArticle(@PathVariable Integer articleId,
                                             @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        return new ResponseEntity<>(favoriteArticleService.favoriteArticle(articleId, currentUser), HttpStatus.OK);
    }
}
