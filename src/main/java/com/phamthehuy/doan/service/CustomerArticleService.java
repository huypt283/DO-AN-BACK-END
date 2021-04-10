package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.ArticleInsertRequest;
import com.phamthehuy.doan.model.request.ArticleUpdateRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface CustomerArticleService {
    List<ArticleResponse> listArticleNotHidden(String roomType, String search,
                                               Integer ward, Integer district, Integer city,
                                               Integer minPrice, Integer maxPrice,
                                               Integer minAcreage, Integer maxAcreage) throws Exception;

    ArticleResponse getArticleBySlug(String slug) throws Exception;

    List<ArticleResponse> getListNewArticle(Integer page, Integer limit) throws Exception;

    //    list bài đăng cá nhân	/customer/article
    //    lọc bài đăng theo trạng thái: chưa duyệt, đang đăng, đã ẩn	/customer/article?status={uncheck/active/hidden}
    //    tìm kiếm bài đăng theo title	/customer/article?title={title}
    List<ArticleResponse> listArticleByEmail(String email, String sort, Long start, Long end,
                                             Integer ward, Integer district, Integer city,
                                             Boolean roommate,
                                             String status, Boolean vip, String search,
                                             Integer minAcreage, Integer maxAcreage,
                                             Integer page, Integer limit);

    //    đăng bài	/customer/article
    ArticleResponse insertArticle(UserDetails currentUser, ArticleInsertRequest articleInsertRequest)
            throws Exception;

    //    sửa bài đăng	/customer/article
    ArticleResponse updateArticle(String email, ArticleUpdateRequest articleUpdateRequest,
                                  Integer id)
            throws BadRequestException;


    //    xóa bài đăng	/customer/article/{id}
    MessageResponse deleteArticle(String email, Integer id) throws BadRequestException;

    //    gia hạn bài đăng	/customer/article/extension/{id}?date={int}
    MessageResponse extensionExp(String email, Integer id, Integer date, String type) throws BadRequestException;

    //    ẩn bài đăng  /customer/article/hidden/{id}
    MessageResponse hideArticle(String email, Integer id) throws BadRequestException;

    //đăng lại bài đăng đã ẩn	/customer/article/post/{id}?days={int}
    MessageResponse showArticle(String email, Integer Id) throws BadRequestException;

    //chi tiết bài đăng	/customer/article/{id}
    ArticleResponse detailArticle(String email, Integer id) throws BadRequestException;
}
