package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Article;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface CustomArticleRepository {
    List<Article> findCustom(String sort, Long start, Long end,
                             Integer ward, Integer district, Integer city,
                             Boolean roommate,
                             String status, Boolean vip, String search,
                             Integer minAcreage, Integer maxAcreage);

    List<Article> findCustomNotHidden(String roomType, String title,
                                      Integer ward, Integer district, Integer city,
                                      Integer minPrice, Integer maxPrice,
                                      Integer minAcreage, Integer maxAcreage);

    List<Article> findCustomByEmail(Long start, Long end, String roomType,
                                    Integer ward, Integer district, Integer city,
                                    String status, Boolean vip, String title,
                                    Integer minAcreage, Integer maxAcreage,
                                    UserDetails currentUser);

}
