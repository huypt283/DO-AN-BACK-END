package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.News;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.model.request.NewsRequest;
import com.phamthehuy.doan.model.request.OffsetBasedPageRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;
import com.phamthehuy.doan.repository.NewsRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.NewsService;
import com.phamthehuy.doan.util.SlugUtil;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl implements NewsService {
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private StaffRepository staffRepository;

    @Override
    public List<NewsResponse> listNews(Integer page, Integer limit) {
        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest((page - 1) * limit, limit,
                Sort.by("timeUpdated").descending().and(Sort.by("timeCreated").descending()));
        return newsRepository.findByDeletedFalse(pageable).stream().map(this::convertToOutputDTO).collect(Collectors.toList());
    }

    @Override
    public List<NewsResponse> listNewsNotHidden() {
        return newsRepository.findByDeletedFalse(Sort.by("timeUpdated").descending().and(Sort.by("timeCreated").descending()))
                .stream().map(this::convertToOutputDTO).collect(Collectors.toList());
    }

    @Override
    public NewsResponse getNewsBySlug(String slug) throws Exception {
        News news = newsRepository.findBySlug(slug);
        validateNews(news);

        if (BooleanUtils.isTrue(news.getDeleted()))
            throw new NotFoundException("Tin tức này đã bị ẩn");

        return this.convertToOutputDTO(news);
    }

    public List<NewsResponse> listAllNews() {
        List<News> newses = newsRepository.findAll();
        return newses.stream().map(this::convertToOutputDTO).collect(Collectors.toList());
    }

    @Override
    public NewsResponse getNewsById(Integer id) throws Exception {
        News news = newsRepository.findByNewId(id);
        validateNews(news);

        return convertToOutputDTO(newsRepository.save(news));
    }

    @Override
    public MessageResponse insertNews(NewsRequest newsRequest, UserDetails currentUser) throws Exception {
        Staff staff = staffRepository.findByEmail(currentUser.getUsername());
        validateStaff(staff);

        News news = new News();
        BeanUtils.copyProperties(newsRequest, news);
        news.setSlug(SlugUtil.makeSlug(newsRequest.getTitle()) + "-" + System.currentTimeMillis());
        news.setStaff(staff);
        news.setTimeUpdated(new Date());

        newsRepository.save(news);
        return new MessageResponse("Thêm tin tức mới thành công");
    }

    @Override
    public MessageResponse updateNewsById(Integer id, NewsRequest newsRequest, UserDetails currentUser) throws Exception {
        Staff staff = staffRepository.findByEmail(currentUser.getUsername());
        validateStaff(staff);

        News news = newsRepository.findByNewId(id);
        validateNews(news);

        BeanUtils.copyProperties(newsRequest, news);
        news.setStaff(staff);
        news.setTimeUpdated(new Date());

        newsRepository.save(news);
        return new MessageResponse("Cập nhật tin tức thành công");
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
        newsResponse.setLastModified(String.format("%s <%s>", news.getStaff().getName(), news.getStaff().getEmail()));
        newsResponse.setTimeUpdated(news.getTimeUpdated() != null ? news.getTimeUpdated() : news.getTimeUpdated());
        List<String> images = Arrays.asList(news.getImages().split(",@"));
        newsResponse.setImages(images);
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
