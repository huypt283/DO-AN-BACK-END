package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface NewsService {
    List<NewsResponse> listNews(Integer page, Integer limit);

    List<NewsResponse> listNewsNotHidden();

    NewsResponse getNewsBySlug(String slug) throws Exception;

    List<NewsResponse> listAllNews();

    NewsResponse getNewsById(Integer id) throws Exception;

    NewsResponse insertNews(NewsInsertRequest newsInsertRequest, UserDetails currentUser) throws Exception;

    NewsResponse updateNewsById(Integer id, NewsUpdateRequest newsUpdateRequest) throws Exception;

    MessageResponse activeNewsById(Integer id) throws Exception;

    MessageResponse hideNewsById(Integer id) throws Exception;

    MessageResponse deleteNewsById(Integer id) throws Exception;
}
