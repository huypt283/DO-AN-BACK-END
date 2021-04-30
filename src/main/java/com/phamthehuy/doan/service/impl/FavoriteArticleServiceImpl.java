package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.FavoriteArticle;
import com.phamthehuy.doan.exception.AccessDeniedException;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.ArticleRepository;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.FavoriteArticleRepository;
import com.phamthehuy.doan.service.FavoriteArticleService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteArticleServiceImpl implements FavoriteArticleService {
    @Autowired
    private FavoriteArticleRepository favoriteArticleRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleServiceImpl articleService;

    @Override
    public List<FavoriteArticle> listFavoriteArticle(String email) throws Exception {
        if (!email.equals("") && email.contains("@"))
            return favoriteArticleRepository.findByCustomer_Email(email);
        return null;
    }

    @Override
    public List<ArticleResponse> listFavoriteArticle(UserDetails currentUser) throws Exception {
        List<FavoriteArticle> favoriteArticles = favoriteArticleRepository.findByCustomer_Email(currentUser.getUsername());
        return favoriteArticles.stream()
                .map(favoriteArticle -> {
                    ArticleResponse articleResponse = articleService.convertToArticleResponse(favoriteArticle.getArticle());
                    articleResponse.setFavorite(true);
                    return articleResponse;
                }).collect(Collectors.toList());
    }

    @Override
    public MessageResponse favoriteArticle(Integer articleId, UserDetails currentUser) throws Exception {
        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
        validateCustomer(customer);

        Article article = articleRepository.findByArticleId(articleId);
        if (article == null) {
            throw new NotFoundException("Bài đăng không tồn tại");
        }

        FavoriteArticle favoriteArticle = favoriteArticleRepository.findByArticleAndCustomer(article, customer);
        if (favoriteArticle != null) {
            favoriteArticleRepository.delete(favoriteArticle);
            return new MessageResponse("Xoá bài đăng đã lưu thành công");
        } else {
            favoriteArticle = new FavoriteArticle();
            favoriteArticle.setCustomer(customer);
            favoriteArticle.setArticle(article);
            favoriteArticleRepository.save(favoriteArticle);
            return new MessageResponse("Lưu bài đăng thành công");
        }
    }

    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new AccessDeniedException("Bạn cần đăng nhập tài khoản khách hàng để sử dụng chức năng này");
        } else if (BooleanUtils.isNotTrue(customer.getEnabled()))
            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
        else if (BooleanUtils.isTrue(customer.getDeleted()))
            throw new AccessDeniedException("Tài khoản này đã bị khoá");
    }
}
