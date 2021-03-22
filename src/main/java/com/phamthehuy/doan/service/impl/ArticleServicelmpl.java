package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.dao.ArticleRepository;
import com.phamthehuy.doan.model.dto.input.ArticleInsertDTO;
import com.phamthehuy.doan.model.dto.input.ArticleUpdateDTO;
import com.phamthehuy.doan.model.dto.output.ArticleOutputDTO;
import com.phamthehuy.doan.model.entity.Article;
import com.phamthehuy.doan.service.ArticleService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleServicelmpl implements ArticleService {
    final
    ArticleRepository reponsitory;

    public ArticleServicelmpl(ArticleRepository reponsitory) {
        this.reponsitory = reponsitory;
    }
    private SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyy/mm/dd");

    @Override
    public List<ArticleOutputDTO> listArticle(Integer page, Integer limit) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<Article> articleList;
        if (page != null && limit != null) {
            Page<Article> pages = reponsitory.findByDeletedFalse(PageRequest.of(page, limit));
            articleList = pages.toList();
        } else
            articleList = reponsitory.findByDeletedFalse();
        List<ArticleOutputDTO> articleOutputDTO = new ArrayList<>();
        for (Article article : articleList) {
            articleOutputDTO.add(modelMapper.map(article, ArticleOutputDTO.class));
        }
        return articleOutputDTO;
    }

    @Override
    public ResponseEntity<?> insertArticle(ArticleInsertDTO articleInsertDTO) {
        try {

            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Article article = modelMapper.map(articleInsertDTO, Article.class);
            Article newArticle = reponsitory.save(article);
            return ResponseEntity.ok(modelMapper.map(newArticle, ArticleOutputDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body( "Insert failed");
        }
    }

    @Override
    public ResponseEntity<?> updateArticle(ArticleUpdateDTO articleUpdateDTO) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            Article article = modelMapper.map(articleUpdateDTO, Article.class);
            Article newArticle = reponsitory.save(article);
            return ResponseEntity.ok(modelMapper.map(newArticle, ArticleOutputDTO.class));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Update failed");
        }
    }
    @Override
    public ResponseEntity<String> deleteArticle(Integer id) {
        Article article = reponsitory.findByArticleIdAndDeletedFalse(id);
        if (article == null) {
            return ResponseEntity.badRequest().body("id" + id + "này không tồn tại");
        } else {
            article.setDeleted(true);
            article.setStatus(true);
            reponsitory.save(article);
        }
        return ResponseEntity.ok("bài viết này đã được ẩn ");
    }

    @Override
    public ResponseEntity<String> activeArticle(Integer id) {
        Optional<Article> articles = reponsitory.findById(id);
        if (!articles.isPresent()) {
            return ResponseEntity.badRequest().body("id " + id + " không tồn tại");
        } else {
            articles.get().setDeleted(false);
            reponsitory.save(articles.get());
        }
        return ResponseEntity.ok("bài viết đã được hiển thị");
    }

    @Override
    public ResponseEntity<?> findOneArticle(Integer id) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            return ResponseEntity.ok(modelMapper.map(reponsitory.findByArticleIdAndDeletedFalse(id),
                    ArticleOutputDTO.class));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("id: " + id + " không tìm thấy");
        }
    }

    @Override
    public List<ArticleOutputDTO> findArticleByTitleAndPhone(String search, Integer page, Integer limit) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        List<Article> articleList;
        if (page != null && limit != null) {
            Page<Article> pages = reponsitory.findByTitleLikeAndDeletedFalse
                    ("%" + search + "%", PageRequest.of(page, limit));
            articleList = pages.toList();
        } else articleList = reponsitory.findByTitleLikeAndDeletedFalse
                ("%" + search + "%");
        List<ArticleOutputDTO> ArticleOutputDTO = new ArrayList<>();
        for (Article article : articleList) {
            ArticleOutputDTO.add(modelMapper.map(article, ArticleOutputDTO.class));
        }
        return ArticleOutputDTO;
    }

    @Override
    public List<ArticleOutputDTO> findArticleByPostTimeDESC(Integer page, Integer limit) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<Article> articleList;
        if (page != null && limit != null) {
            Page<Article> pages = reponsitory.findByDeletedFalseOrderByPostTimeDesc(PageRequest.of(page, limit));
            articleList = pages.toList();
        } else
            articleList = reponsitory.findByDeletedFalseOrderByPostTimeDesc();
        List<ArticleOutputDTO> articleOutputDTO = new ArrayList<>();
        for (Article article : articleList) {
            articleOutputDTO.add(modelMapper.map(article, ArticleOutputDTO.class));
        }
        return articleOutputDTO;
    }

    @Override
    public List<ArticleOutputDTO> findArticleByPostTimeAsc(Integer page, Integer limit) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        List<Article> articleList;
        if (page != null && limit != null) {
            Page<Article> pages = reponsitory.findByDeletedFalseOrderByPostTimeAsc(PageRequest.of(page, limit));
            articleList = pages.toList();
        } else
            articleList = reponsitory.findByDeletedFalseOrderByPostTimeAsc();
        List<ArticleOutputDTO> articleOutputDTOS = new ArrayList<>();
        for (Article article : articleList) {
            articleOutputDTOS.add(modelMapper.map(article, ArticleOutputDTO.class));
        }
        return articleOutputDTOS;
    }

    @Override
    public List<ArticleOutputDTO> ListAriticleStatusTrue(Integer page, Integer limit) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        List<Article> articleList;
        if(page != null && limit != null){
            Page<Article> pages = reponsitory.findByDeletedTrue(PageRequest.of(page,limit));
            articleList= pages.toList();
        }else
            articleList = reponsitory.findByDeletedTrue();
        List<ArticleOutputDTO> articleOutputDTOS = new ArrayList<>();
        for (Article article: articleList){
            articleOutputDTOS.add(modelMapper.map(article,ArticleOutputDTO.class));
        }
        return articleOutputDTOS;
    }


}
