package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.ArticleInsertRequest;
import com.phamthehuy.doan.model.request.ArticleUpdateRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;

import java.util.List;

public interface CustomerArticleService {
    //    list bài đăng cá nhân	/customer/article
    //    lọc bài đăng theo trạng thái: chưa duyệt, đang đăng, đã ẩn	/customer/article?status={uncheck/active/hidden}
    //    tìm kiếm bài đăng theo title	/customer/article?title={title}
    List<ArticleResponse> listArticle(String email, String sort, Long start, Long end,
                                      Integer ward, Integer district, Integer city,
                                      Boolean roommate,
                                      String status, Boolean vip, String search,
                                      Integer minAcreage, Integer maxAcreage,
                                      Integer page, Integer limit);

    //    đăng bài	/customer/article
    ArticleResponse insertArticle(String email, ArticleInsertRequest articleInsertRequest)
            throws CustomException;

    //    sửa bài đăng	/customer/article
    ArticleResponse updateArticle(String email, ArticleUpdateRequest articleUpdateRequest,
                                  Integer id)
            throws CustomException;

    //    ẩn bài đăng	/customer/article/hidden/{id}
    MessageResponse hiddenArticle(String email, Integer id) throws CustomException;

    //    xóa bài đăng	/customer/article/{id}
    MessageResponse deleteArticle(String email, Integer id) throws CustomException;

    //    gia hạn bài đăng	/customer/article/extension/{id}?date={int}
    MessageResponse extensionExp(String email, Integer id, Integer date, String type) throws CustomException;

    //đăng lại bài đăng đã ẩn	/customer/article/post/{id}?days={int}
    MessageResponse postOldArticle(String email, Integer Id, Integer date, String type) throws CustomException;

    //chi tiết bài đăng	/customer/article/{id}
    ArticleResponse detailArticle(String email, Integer id) throws CustomException;
}
