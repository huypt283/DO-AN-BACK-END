package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.ArticleStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleStatisticRepository extends JpaRepository<ArticleStatistic, Integer> {
    ArticleStatistic findByTime(String time);
}
