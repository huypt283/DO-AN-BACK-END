package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer>, CustomArticleRepository{
    Article findByDeletedTrueAnAndArticleId(Integer id);

    Article findByArticleId(Integer id);

    List<Article> findByDeletedFalse();
}
