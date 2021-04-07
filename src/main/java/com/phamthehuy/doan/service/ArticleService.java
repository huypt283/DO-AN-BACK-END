package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ArticleService {
    List<ArticleResponse> listArticle(
            String sort,
            Long start, Long end,
            Integer ward,
            Integer district,
            Integer city,
            Boolean roommate,
            String status,
            Boolean vip,
            String search,
            Integer minAcreage, Integer maxAcreage,
            Integer page,
            Integer limit
    );

//    contact với khách hàng (gửi mail cho khách hàng về bài viết này)	/admin/article/contact/{id}
    MessageResponse contactToCustomer(Integer id, ContactCustomerRequest contactCustomerRequest,
                                      HttpServletRequest request) throws CustomException;
//    duyệt bài đăng (hiện) (gửi mail)	/admin/article/active/{id}
    MessageResponse activeArticle(Integer id, HttpServletRequest request) throws CustomException;
//    ẩn bài đăng (gửi mail)	/admin/article/block/{id}
    MessageResponse hiddenArticle(Integer id, String reason, HttpServletRequest request) throws CustomException;

    //chi tiết bài đăng
    ArticleResponse detailArticle(Integer id) throws CustomException;
}
