package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.News;
import com.phamthehuy.doan.model.request.OffsetBasedPageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Integer> {
    List<News> findByDeletedFalse(OffsetBasedPageRequest pageable);

    List<News> findByDeletedFalse(Sort sort);

    News findByNewId(Integer newsId);

    News findBySlug(String slug);

    Page<News> findByTitleLikeAndDeleted(String title, Boolean deleted, Pageable pageable);

    Page<News> findByTitleLike(String title, Pageable pageable);
}
