package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface FavoriteArticleService {
    List<ArticleResponse> listFavoriteArticle(UserDetails currentUser) throws Exception;

    MessageResponse favoriteArticle(Integer articleId, UserDetails currentUser) throws Exception;
}
