package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.News;
import com.phamthehuy.doan.model.request.OffsetBasedPageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Integer> {
    @Query("select n from News n where n.deleted = false")
    List<News> findByDeletedFalse(OffsetBasedPageRequest pageable);

    List<News> findByDeletedFalse(Sort sort);

    News findByNewId(Integer newsId);

    News findBySlug(String slug);
}
