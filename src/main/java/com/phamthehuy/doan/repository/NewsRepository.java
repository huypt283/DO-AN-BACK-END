package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, Integer> {
    Page<News> findByTitleLikeAndDeleted(String title, Boolean deleted, Pageable pageable);

    Page<News> findByTitleLike(String title, Pageable pageable);
}
