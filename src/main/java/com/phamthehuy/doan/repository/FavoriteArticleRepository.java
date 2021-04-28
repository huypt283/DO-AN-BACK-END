package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.FavoriteArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteArticleRepository extends JpaRepository<FavoriteArticle, Integer> {
    List<FavoriteArticle> findByCustomer_Email(String email);

    FavoriteArticle findByArticleAndCustomer(Article article, Customer customer);
}
