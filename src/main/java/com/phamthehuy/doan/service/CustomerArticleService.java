package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.request.ArticleInsertRequest;
import com.phamthehuy.doan.model.request.ArticleUpdateRequest;
import com.phamthehuy.doan.model.request.ExtendArticleExpRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface CustomerArticleService {
    List<ArticleResponse> listArticleNotHidden(String roomType, String title,
                                               Integer ward, Integer district, Integer city,
                                               Integer minPrice, Integer maxPrice,
                                               Integer minAcreage, Integer maxAcreage) throws Exception;

    ArticleResponse getArticleBySlug(String slug) throws Exception;

    List<ArticleResponse> getListNewArticle(Integer page, Integer limit) throws Exception;

    List<ArticleResponse> listCurrentUserArticleByEmail(Long start, Long end, String roomType,
                                                        Integer ward, Integer district, Integer city,
                                                        String status, Boolean vip, String title,
                                                        Integer minAcreage, Integer maxAcreage,
                                                        UserDetails currentUser) throws Exception;

    ArticleResponse detailArticle(UserDetails currentUser, Integer id) throws Exception;

    ArticleResponse insertArticle(UserDetails currentUser, ArticleInsertRequest articleInsertRequest)
            throws Exception;

    ArticleResponse updateArticle(UserDetails currentUser, Integer id, ArticleUpdateRequest articleUpdateRequest)
            throws Exception;

    MessageResponse deleteArticle(UserDetails currentUser, Integer id) throws Exception;

    MessageResponse extendExp(UserDetails currentUser, Integer id, ExtendArticleExpRequest extendArticleExpRequest)
            throws Exception;

    MessageResponse hideArticle(UserDetails currentUser, Integer id) throws Exception;

    MessageResponse showArticle(UserDetails currentUser, Integer id) throws Exception;
}
