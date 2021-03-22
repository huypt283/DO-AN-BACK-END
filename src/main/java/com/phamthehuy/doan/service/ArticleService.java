package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.dto.input.ArticleInsertDTO;
import com.phamthehuy.doan.model.dto.input.ArticleUpdateDTO;
import com.phamthehuy.doan.model.dto.output.ArticleOutputDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ArticleService {
    //danh sách bài viêt Get/admin/articles
    List<ArticleOutputDTO> listArticle(Integer page, Integer limit);

    //thêm bài viết Post/admin/articles
    // lưa ý các bài viết được thêm vào thì mặc ddinjhj của nó là true có nghĩa là chưa được hiển thị
    ResponseEntity<?> insertArticle(ArticleInsertDTO articleInsertDTO);

    //cập nhật bài viết Put/admin/articles
    ResponseEntity<?> updateArticle(ArticleUpdateDTO articleUpdateDTO);

    //xóa bài viết Delete/admin/articles/{id}
    ResponseEntity<String> deleteArticle(Integer id);

    //duyệt bài viết  Get/admin/articles/active/{id}
    ResponseEntity<String> activeArticle(Integer id);

    //xem bài viết Get/admin/articles/{id}
    ResponseEntity<?> findOneArticle(Integer id);

    // tìm kiếm bài viêt theo nội dung Get/admin/articles?seach=?
    List<ArticleOutputDTO> findArticleByTitleAndPhone(String search, Integer page, Integer limit);

    // tìm kiếm bài viết theo  thời gian tăng dần Get/admin/articles/post-time=desc"//////
    List<ArticleOutputDTO> findArticleByPostTimeDESC(Integer page, Integer limit);

    //tìm kiếm bào viết theo thời gian giảm dần  Get/admin/articles/post-time=asc//////
    List<ArticleOutputDTO> findArticleByPostTimeAsc(Integer page, Integer limit);

    // danh sach bài đăng theo trạng thái GET/admin/articles/status-true
    List<ArticleOutputDTO> ListAriticleStatusTrue(Integer page, Integer limit);

}
