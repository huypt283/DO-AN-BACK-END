package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;

import java.util.List;

public interface NewspaperService {
    //list tin tức
    List<NewsResponse> listNewspaper(String sort, Boolean hidden, String title,
                                     Integer page, Integer limit);

    //tin tức details
    NewsResponse findOneNewspaper(Integer id) throws CustomException;

    //đăng tin tức
    NewsResponse insertNewspaper(NewsInsertRequest newsInsertRequest) throws CustomException;

//    sửa tin tức
    NewsResponse updateNewspaper(NewsUpdateRequest newsUpdateRequest,
                                 Integer id) throws CustomException;
//    ẩn 1 tin tức
    MessageResponse hiddenNewspaper(Integer id) throws CustomException;
//    hiện 1 tin tức
    MessageResponse activeNewspaper(Integer id) throws CustomException;
//    xóa tin tức
    MessageResponse deleteNewspaper(Integer id) throws CustomException;
}
