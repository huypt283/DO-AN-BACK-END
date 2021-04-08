package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;

import java.util.List;

public interface NewsService {
    //list tin tức
    List<NewsResponse> listNews(String sort, Boolean hidden, String title,
                                Integer page, Integer limit);

    //tin tức details
    NewsResponse findNewsById(Integer id) throws BadRequestException;

    //đăng tin tức
    NewsResponse insertNews(NewsInsertRequest newsInsertRequest) throws BadRequestException;

//    sửa tin tức
    NewsResponse updateNews(NewsUpdateRequest newsUpdateRequest,
                            Integer id) throws BadRequestException;
//    ẩn 1 tin tức
    MessageResponse hideNews(Integer id) throws BadRequestException;
//    hiện 1 tin tức
    MessageResponse activeNews(Integer id) throws BadRequestException;
//    xóa tin tức
    MessageResponse delteNews(Integer id) throws BadRequestException;
}
