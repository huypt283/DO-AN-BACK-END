package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticlesRepository extends JpaRepository<Article, Integer> {

}

