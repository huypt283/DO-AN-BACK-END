package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.News;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;
import com.phamthehuy.doan.repository.NewsRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.NewsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl implements NewsService {
    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private StaffRepository staffRepository;

    public List<NewsResponse> listAllNews() {
        List<News> newses = newsRepository.findAll();
        return newses.stream().map(this::convertToOutputDTO).collect(Collectors.toList());
    }

    @Override
    public NewsResponse findNewsById(Integer id) throws Exception {
        News news = newsRepository.findByNewId(id);
        validateNews(news);

        return convertToOutputDTO(newsRepository.save(news));
    }

    @Override
    public NewsResponse insertNews(NewsInsertRequest newsInsertRequest, UserDetails currentUser) throws Exception {
        Staff staff = staffRepository.findByEmail(currentUser.getUsername());
        validateStaff(staff);

        News news = new News();
        BeanUtils.copyProperties(newsInsertRequest, news);
        news.setStaff(staff);
        news.setTimeUpdated(new Date());
        return convertToOutputDTO(newsRepository.save(news));
    }

    @Override
    public NewsResponse updateNewsById(Integer id, NewsUpdateRequest newsUpdateRequest) throws Exception {
        News news = newsRepository.findByNewId(id);
        validateNews(news);

        BeanUtils.copyProperties(newsUpdateRequest, news);
        news.setTimeUpdated(new Date());
        return convertToOutputDTO(newsRepository.save(news));
    }

    @Override
    public MessageResponse hideNewsById(Integer id) throws Exception {
        News news = newsRepository.findByNewId(id);
        validateNews(news);

        news.setDeleted(true);
        newsRepository.save(news);
        return new MessageResponse("Ẩn bài viết thành công");
    }

    @Override
    public MessageResponse activeNewsById(Integer id) throws Exception {
        News news = newsRepository.findByNewId(id);
        validateNews(news);

        news.setDeleted(false);
        newsRepository.save(news);
        return new MessageResponse("Hiện bài viết thành công");
    }

    @Override
    public MessageResponse deleteNewsById(Integer id) throws Exception {
        try {
            newsRepository.deleteById(id);
            return new MessageResponse("Xoá bài viết thành công");
        } catch (Exception e) {
            throw new NotFoundException("Bài viết không tồn tại");
        }
    }

    private NewsResponse convertToOutputDTO(News news) {
        NewsResponse newsResponse = new NewsResponse();
        BeanUtils.copyProperties(news, newsResponse);
        newsResponse.setAuthor(news.getStaff().getName() + " (" + news.getStaff().getEmail() + ")");
        newsResponse.setTimeUpdated(news.getTimeUpdated() != null ? news.getTimeUpdated() : news.getTimeUpdated());
        return newsResponse;
    }

    private void validateStaff(Staff staff) {
        if (staff == null)
            throw new NotFoundException("Nhân viên không tồn tại");
    }

    private void validateNews(News news) {
        if (news == null)
            throw new NotFoundException("Bài viết không tồn tại");
    }
}
