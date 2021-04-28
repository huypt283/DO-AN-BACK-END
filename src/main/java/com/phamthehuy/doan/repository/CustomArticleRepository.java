package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.entity.FavoriteArticle;
import com.phamthehuy.doan.entity.Ward;
import com.phamthehuy.doan.model.request.OffsetBasedPageRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Set;

public interface CustomArticleRepository {
    List<Article> findCustomNotHidden(String roomType, String title,
                                      Integer ward, Integer district, Integer city,
                                      Integer minPrice, Integer maxPrice,
                                      Integer minAcreage, Integer maxAcreage);

    List<Article> findCustomByEmail(Long start, Long end, String roomType,
                                    Integer ward, Integer district, Integer city,
                                    String status, Boolean vip, String title,
                                    Integer minAcreage, Integer maxAcreage,
                                    UserDetails currentUser);

    List<Article> suggestion(Set<FavoriteArticle> favoriteArticles);
}
