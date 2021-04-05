package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.Article;

import java.util.List;

public interface CustomArticleRepository {
    List<Article> findCustom(String sort, Long start, Long end,
                             Integer ward, Integer district, Integer city,
                             Boolean roommate,
                             String status, Boolean vip, String search,
                             Integer minAcreage, Integer maxAcreage,
                             Integer page, Integer limit);

    List<Article> findCustomAndEmail(String email, String sort, Long start, Long end,
                                     Integer ward, Integer district, Integer city,
                                     Boolean roommate,
                                     String status, Boolean vip, String search,
                                     Integer minAcreage, Integer maxAcreage,
                                     Integer page, Integer limit);

}
