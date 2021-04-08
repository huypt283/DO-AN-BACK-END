package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Article;

import java.util.List;

public interface CustomArticleRepository {
    List<Article> findCustom(String sort, Long start, Long end,
                             Integer ward, Integer district, Integer city,
                             Boolean roommate,
                             String status, Boolean vip, String search,
                             Integer minAcreage, Integer maxAcreage);

    List<Article> findCustomNotHidden(Long start, Long end,
                             Integer ward, Integer district, Integer city,
                             Boolean roommate, Boolean vip, String search,
                             Integer minAcreage, Integer maxAcreage);

    List<Article> findCustomByEmail(String email, String sort, Long start, Long end,
                                     Integer ward, Integer district, Integer city,
                                     Boolean roommate,
                                     String status, Boolean vip, String search,
                                     Integer minAcreage, Integer maxAcreage,
                                     Integer page, Integer limit);

}
