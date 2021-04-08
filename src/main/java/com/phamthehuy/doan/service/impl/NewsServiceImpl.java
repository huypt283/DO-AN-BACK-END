package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.repository.NewsRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;
import com.phamthehuy.doan.entity.News;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.service.NewsService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NewsServiceImpl implements NewsService {
    final
    NewsRepository newsRepository;

    final
    StaffRepository staffRepository;

    public NewsServiceImpl(NewsRepository newsRepository, StaffRepository staffRepository) {
        this.newsRepository = newsRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public List<NewsResponse> listNews(String sort, Boolean hidden, String title,
                                       Integer page, Integer limit) {
        if (title == null) title = "";
        Page<News> newspaperPage;
        if (hidden != null) {
            if (sort != null && sort.equals("asc")) {
                newspaperPage = newsRepository.
                        findByTitleLikeAndDeleted("%" + title + "%", hidden,
                                PageRequest.of(page, limit, Sort.by("timeCreated").ascending()));
            } else {
                newspaperPage = newsRepository.
                        findByTitleLikeAndDeleted("%" + title + "%", hidden,
                                PageRequest.of(page, limit, Sort.by("timeCreated").descending()));
            }
        } else {
            if (sort != null && sort.equals("asc")) {
                newspaperPage = newsRepository.
                        findByTitleLike("%" + title + "%",
                                PageRequest.of(page, limit, Sort.by("timeCreated").ascending()));
            } else {
                newspaperPage = newsRepository.
                        findByTitleLike("%" + title + "%",
                                PageRequest.of(page, limit, Sort.by("timeCreated").descending()));
            }
        }

        List<News> newsList = newspaperPage.toList();

        List<NewsResponse> newsResponseList = new ArrayList<>();
        for (News news : newsList) {
            newsResponseList.add(convertToOutputDTO(news));
        }
        return newsResponseList;
    }

    @Override
    public NewsResponse findNewsById(Integer id) throws BadRequestException {
        Optional<News> newspaperOptional = newsRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            return convertToOutputDTO(newspaperOptional.get());
        } else {
            throw new BadRequestException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public NewsResponse insertNews(NewsInsertRequest newsInsertRequest) throws BadRequestException {
        Optional<Staff> staffOptional = staffRepository.findById(newsInsertRequest.getStaffId());
        if(!staffOptional.isPresent())
            throw new BadRequestException("Nhân viên với id " + newsInsertRequest.getStaffId() + " không tồn tại");

        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            News news = modelMapper.map(newsInsertRequest, News.class);
            news.setStaff(staffOptional.get());
            return convertToOutputDTO(newsRepository.save(news));
        } catch (Exception e) {
            throw new BadRequestException("Thêm mới thất bại");
        }
    }

    @Override
    public NewsResponse updateNews(NewsUpdateRequest newsUpdateRequest,
                                   Integer id) throws BadRequestException {
        Optional<Staff> staffOptional = staffRepository.findById(newsUpdateRequest.getStaffId());
        if (!staffOptional.isPresent())
            throw new BadRequestException("Nhân viên với id " + newsUpdateRequest.getStaffId() + " không tồn tại");
        Optional<News> newspaperOptional = newsRepository.findById(id);
        if (!newspaperOptional.isPresent())
            throw new BadRequestException("Bản tin với id " + id + " không tồn tại");
        try {
                News news = newspaperOptional.get();
                news.setTitle(newsUpdateRequest.getTitle());
                news.setContent(newsUpdateRequest.getContent());
                news.setImage(newsUpdateRequest.getImage());
                news.setStaff(staffOptional.get());
                news.setTimeCreated(new Date());
                return convertToOutputDTO(newsRepository.save(news));
        } catch (Exception e) {
            throw new BadRequestException("Cập nhật thất bại");
        }
    }

    @Override
    public MessageResponse hideNews(Integer id) throws BadRequestException {
        Optional<News> newspaperOptional = newsRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            News news = newspaperOptional.get();
            news.setDeleted(true);
            newsRepository.save(news);
            return new MessageResponse("Ẩn bài viết thành công");
        } else {
            throw new BadRequestException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public MessageResponse activeNews(Integer id) throws BadRequestException {
        Optional<News> newspaperOptional = newsRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            News news = newspaperOptional.get();
            news.setDeleted(false);
            newsRepository.save(news);
            return new MessageResponse("Hiện bài viết thành công");
        } else {
            throw new BadRequestException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public MessageResponse delteNews(Integer id) throws BadRequestException {
        try {
            newsRepository.deleteById(id);
            return new MessageResponse("Xoá bài viết thành công");
        } catch (Exception e) {
            throw new BadRequestException("Tin tức với id " + id + " không tồn tại");
        }
    }

    public NewsResponse convertToOutputDTO(News news) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        NewsResponse newsResponse = modelMapper.map(news, NewsResponse.class);
        newsResponse.setAuthor(news.getStaff().getName() + " (" + news.getStaff().getEmail() + ")");
        newsResponse.setLastUpdatedTime(news.getTimeCreated());
        return newsResponse;
    }
}
