package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.model.request.OffsetBasedPageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer>, CustomArticleRepository{
    Article findByArticleId(Integer id);

    Article findBySlug(String slug);

    List<Article> findByDeletedFalse();

    @Query("select a from Article a where a.deleted = false")
    List<Article> findByDeletedFalse(OffsetBasedPageRequest pageable);
}
