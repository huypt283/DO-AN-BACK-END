package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.ContactCustomerRequest;
import com.phamthehuy.doan.model.response.ArticleResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AdminArticleService {
    List<ArticleResponse> listAllArticle();

    List<ArticleResponse> listArticle(String sort, Long start, Long end,
                                      Integer ward, Integer district, Integer city,
                                      Boolean roommate, String status, Boolean vip,
                                      String search, Integer minAcreage, Integer maxAcreage,
                                      Integer page, Integer limit
    );

    ArticleResponse detailArticle(Integer id) throws Exception;

    MessageResponse contactToCustomer(Integer id, UserDetails admin,
                                      ContactCustomerRequest contactCustomerRequest) throws Exception;

    MessageResponse activeArticle(Integer id, UserDetails admin) throws Exception;

    MessageResponse hideArticle(Integer id, UserDetails admin, String reason) throws Exception;
}
