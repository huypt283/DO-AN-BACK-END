
package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ArticleReponsitory extends JpaRepository<Article, Integer> {
    // danh sách bài viết có trạng thái được duyệt
    Page<Article> findByDeletedFalse(Pageable pageable);

    List<Article> findByDeletedFalse();

    // tìm kiếm bài viết theo tiêu đề
    Page<Article> findByTitleLikeAndDeletedFalse(String title, Pageable pageable);

    List<Article> findByTitleLikeAndDeletedFalse(String title);

    // tìm kiếm theo id
    Article findByArticleIdAndDeletedFalse(Integer articleId);

    // tìm kiếm bài viết theo thời gian tăng dần
    Page<Article> findByDeletedFalseOrderByPostTimeAsc(Pageable pageable);

    List<Article> findByDeletedFalseOrderByPostTimeAsc();

    // tìm kiếm các bài viết có trạng thái chưa được duyệt
    Page<Article> findByDeletedTrue(Pageable pageable);
    List<Article> findByDeletedTrue();

    /// tìm kiếm bài viết theo thời gian giảm dần
    Page<Article> findByDeletedFalseOrderByPostTimeDesc(Pageable pageable);
    List<Article> findByDeletedFalseOrderByPostTimeDesc();

    List<Article> findByStatus(Boolean status);
    List<Article> findByPostTimeGreaterThanEqualAndPostTimeIsLessThanEqual
            (Date minDate, Date maxDate);

    List<Article> findByWard_District_City_CityIdAndWard_District_DistrictIdAndWard_WardId
            (Integer cityId, Integer districtId, Integer wardId);

    List<Article> findByWard_District_City_CityIdAndWard_District_DistrictId
            (Integer cityId, Integer districtId);

    List<Article> findByWard_District_City_CityId
            (Integer cityId);

    List<Article> findByTitleLikeOrPhoneLike(String title, String phone);
}
