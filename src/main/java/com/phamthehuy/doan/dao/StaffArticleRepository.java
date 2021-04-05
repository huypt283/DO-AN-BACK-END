package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.StaffArticle;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffArticleRepository extends JpaRepository<StaffArticle, Integer> {
    StaffArticle findFirstByArticle_ArticleId(Integer id, Sort sort);
}
