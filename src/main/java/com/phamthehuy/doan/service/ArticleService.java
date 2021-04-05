package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.dto.input.ContactCustomerDTO;
import com.phamthehuy.doan.model.dto.output.ArticleOutputDTO;
import com.phamthehuy.doan.model.dto.output.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ArticleService {
    List<ArticleOutputDTO> listArticle(
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
    Message contactToCustomer(Integer id, ContactCustomerDTO contactCustomerDTO,
                              HttpServletRequest request) throws CustomException;
//    duyệt bài đăng (hiện) (gửi mail)	/admin/article/active/{id}
    Message activeArticle(Integer id, HttpServletRequest request) throws CustomException;
//    ẩn bài đăng (gửi mail)	/admin/article/block/{id}
    Message hiddenArticle(Integer id, String reason, HttpServletRequest request) throws CustomException;

    //chi tiết bài đăng
    ArticleOutputDTO detailArticle(Integer id) throws CustomException;
}
