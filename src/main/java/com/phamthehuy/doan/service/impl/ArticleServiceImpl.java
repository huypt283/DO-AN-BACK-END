package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.entity.FavoriteArticle;
import com.phamthehuy.doan.entity.StaffArticle;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.repository.StaffArticleRepository;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArticleServiceImpl {
//    @Autowired
//    private StaffArticleRepository staffArticleRepository;

    public ArticleResponse convertToArticleResponse(Article article) {
        ArticleResponse articleResponse = new ArticleResponse();
        BeanUtils.copyProperties(article, articleResponse);

        articleResponse.setCreateTime(article.getTimeCreated());
        articleResponse.setLastUpdateTime(article.getTimeUpdated());
        if (article.getDeleted() != null) {
            if (article.getDeleted())
                articleResponse.setStatus("Đã ẩn");
            else
                articleResponse.setStatus("Đang hiển thị");
        } else articleResponse.setStatus("Chưa duyệt");

//        StaffArticle staffArticle = staffArticleRepository.
//                findFirstByArticle_ArticleId(article.getArticleId(), Sort.by("time").descending());
//
//        if (staffArticle != null && article.getDeleted() != null) {
//            Map<String, String> moderator = new HashMap<>();
//            moderator.put("staffId", staffArticle.getStaff().getStaffId() + "");
//            moderator.put("name", staffArticle.getStaff().getName());
//            moderator.put("email", staffArticle.getStaff().getEmail());
//            articleResponse.setModerator(moderator);
//        }

        Map<String, String> customer = new HashMap<>();
        customer.put("customerId", article.getCustomer().getCustomerId() + "");
        customer.put("name", article.getCustomer().getName());
        customer.put("email", article.getCustomer().getEmail());
        customer.put("phone", article.getCustomer().getPhone());
        articleResponse.setCustomer(customer);

        articleResponse.setExpDate(article.getExpTime());

        articleResponse.setLastUpdateTime(article.getTimeUpdated() != null ? article.getTimeUpdated() : article.getTimeCreated());
        List<String> images = Arrays.asList(article.getImages().split(",@"));
        articleResponse.setImages(images);

        Map<String, String> location = new HashMap<>();
        location.put("wardId", article.getWard().getWardId() + "");
        location.put("wardName", article.getWard().getWardName());
        location.put("districtId", article.getWard().getDistrict().getDistrictId() + "");
        location.put("districtName", article.getWard().getDistrict().getDistrictName());
        location.put("cityId", article.getWard().getDistrict().getCity().getCityId() + "");
        location.put("cityName", article.getWard().getDistrict().getCity().getCityName());
        location.put("homeAddress", article.getAddress().split(",")[0]);
        location.put("address", article.getAddress());
        articleResponse.setLocation(location);

        return articleResponse;
    }

    public ArticleResponse convertToArticleResponseWithFavorite(Article article, List<FavoriteArticle> favoriteArticles) {
        ArticleResponse articleResponse = this.convertToArticleResponse(article);

        final Integer articleId = article.getArticleId();
        if (!Collections.isEmpty(favoriteArticles)) {
            Optional<FavoriteArticle> favoriteArticle = favoriteArticles.stream().filter(fa -> fa.getArticle().getArticleId().equals(articleId)).findFirst();
            if (favoriteArticle.isPresent()) {
                articleResponse.setFavorite(true);
            }
        }

        return articleResponse;
    }
}
