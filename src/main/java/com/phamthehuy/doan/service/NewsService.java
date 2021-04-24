package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.request.NewsRequest;
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

    NewsResponse insertNews(NewsRequest newsRequest, UserDetails currentUser) throws Exception;

    NewsResponse updateNewsById(Integer id, NewsRequest newsRequest) throws Exception;

    MessageResponse activeNewsById(Integer id) throws Exception;

    MessageResponse hideNewsById(Integer id) throws Exception;

    MessageResponse deleteNewsById(Integer id) throws Exception;
}
