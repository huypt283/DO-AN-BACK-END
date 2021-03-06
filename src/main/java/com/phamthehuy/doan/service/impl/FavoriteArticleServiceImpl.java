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
            throw new NotFoundException("B??i ????ng kh??ng t???n t???i");
        }

        FavoriteArticle favoriteArticle = favoriteArticleRepository.findByArticleAndCustomer(article, customer);
        if (favoriteArticle != null) {
            favoriteArticleRepository.delete(favoriteArticle);
            return new MessageResponse("Xo?? b??i ????ng ???? l??u th??nh c??ng");
        } else {
            favoriteArticle = new FavoriteArticle();
            favoriteArticle.setCustomer(customer);
            favoriteArticle.setArticle(article);
            favoriteArticleRepository.save(favoriteArticle);
            return new MessageResponse("L??u b??i ????ng th??nh c??ng");
        }
    }

    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new AccessDeniedException("B???n c???n ????ng nh???p t??i kho???n kh??ch h??ng ????? s??? d???ng ch???c n??ng n??y");
        } else if (BooleanUtils.isNotTrue(customer.getEnabled()))
            throw new AccessDeniedException("T??i kho???n n??y ch??a ???????c k??ch ho???t");
        else if (BooleanUtils.isTrue(customer.getDeleted()))
            throw new AccessDeniedException("T??i kho???n n??y ???? b??? kho??");
    }
}
