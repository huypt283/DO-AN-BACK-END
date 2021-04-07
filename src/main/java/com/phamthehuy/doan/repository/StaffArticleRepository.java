package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.StaffArticle;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffArticleRepository extends JpaRepository<StaffArticle, Integer> {
    StaffArticle findFirstByArticle_ArticleId(Integer id, Sort sort);
}
