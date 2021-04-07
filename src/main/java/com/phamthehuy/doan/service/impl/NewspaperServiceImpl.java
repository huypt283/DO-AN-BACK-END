package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.repository.NewspaperRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;
import com.phamthehuy.doan.entity.Newspaper;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.service.NewspaperService;
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
public class NewspaperServiceImpl implements NewspaperService {
    final
    NewspaperRepository newspaperRepository;

    final
    StaffRepository staffRepository;

    public NewspaperServiceImpl(NewspaperRepository newspaperRepository, StaffRepository staffRepository) {
        this.newspaperRepository = newspaperRepository;
        this.staffRepository = staffRepository;
    }

    @Override
    public List<NewsResponse> listNewspaper(String sort, Boolean hidden, String title,
                                            Integer page, Integer limit) {
        if (title == null) title = "";
        Page<Newspaper> newspaperPage;
        if (hidden != null) {
            if (sort != null && sort.equals("asc")) {
                newspaperPage = newspaperRepository.
                        findByTitleLikeAndDeleted("%" + title + "%", hidden,
                                PageRequest.of(page, limit, Sort.by("timeCreated").ascending()));
            } else {
                newspaperPage = newspaperRepository.
                        findByTitleLikeAndDeleted("%" + title + "%", hidden,
                                PageRequest.of(page, limit, Sort.by("timeCreated").descending()));
            }
        } else {
            if (sort != null && sort.equals("asc")) {
                newspaperPage = newspaperRepository.
                        findByTitleLike("%" + title + "%",
                                PageRequest.of(page, limit, Sort.by("timeCreated").ascending()));
            } else {
                newspaperPage = newspaperRepository.
                        findByTitleLike("%" + title + "%",
                                PageRequest.of(page, limit, Sort.by("timeCreated").descending()));
            }
        }

        List<Newspaper> newspaperList = newspaperPage.toList();

        List<NewsResponse> newsResponseList = new ArrayList<>();
        for (Newspaper newspaper : newspaperList) {
            newsResponseList.add(convertToOutputDTO(newspaper));
        }
        return newsResponseList;
    }

    @Override
    public NewsResponse findOneNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            return convertToOutputDTO(newspaperOptional.get());
        } else {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public NewsResponse insertNewspaper(NewsInsertRequest newsInsertRequest) throws CustomException {
        Optional<Staff> staffOptional = staffRepository.findById(newsInsertRequest.getStaffId());
        if(!staffOptional.isPresent())
            throw new CustomException("Nhân viên với id " + newsInsertRequest.getStaffId() + " không tồn tại");

        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Newspaper newspaper = modelMapper.map(newsInsertRequest, Newspaper.class);
            newspaper.setStaff(staffOptional.get());
            return convertToOutputDTO(newspaperRepository.save(newspaper));
        } catch (Exception e) {
            throw new CustomException("Thêm mới thất bại");
        }
    }

    @Override
    public NewsResponse updateNewspaper(NewsUpdateRequest newsUpdateRequest,
                                        Integer id) throws CustomException {
        Optional<Staff> staffOptional = staffRepository.findById(newsUpdateRequest.getStaffId());
        if (!staffOptional.isPresent())
            throw new CustomException("Nhân viên với id " + newsUpdateRequest.getStaffId() + " không tồn tại");
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (!newspaperOptional.isPresent())
            throw new CustomException("Bản tin với id " + id + " không tồn tại");
        try {
                Newspaper newspaper = newspaperOptional.get();
                newspaper.setTitle(newsUpdateRequest.getTitle());
                newspaper.setContent(newsUpdateRequest.getContent());
                newspaper.setImage(newsUpdateRequest.getImage());
                newspaper.setStaff(staffOptional.get());
                newspaper.setTimeCreated(new Date());
                return convertToOutputDTO(newspaperRepository.save(newspaper));
        } catch (Exception e) {
            throw new CustomException("Cập nhật thất bại");
        }
    }

    @Override
    public MessageResponse hiddenNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setDeleted(true);
            newspaperRepository.save(newspaper);
            return new MessageResponse("Ẩn bài viết thành công");
        } else {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public MessageResponse activeNewspaper(Integer id) throws CustomException {
        Optional<Newspaper> newspaperOptional = newspaperRepository.findById(id);
        if (newspaperOptional.isPresent()) {
            Newspaper newspaper = newspaperOptional.get();
            newspaper.setDeleted(false);
            newspaperRepository.save(newspaper);
            return new MessageResponse("Hiện bài viết thành công");
        } else {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    @Override
    public MessageResponse deleteNewspaper(Integer id) throws CustomException {
        try {
            newspaperRepository.deleteById(id);
            return new MessageResponse("Xoá bài viết thành công");
        } catch (Exception e) {
            throw new CustomException("Tin tức với id " + id + " không tồn tại");
        }
    }

    public NewsResponse convertToOutputDTO(Newspaper newspaper) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        NewsResponse newsResponse = modelMapper.map(newspaper, NewsResponse.class);
        newsResponse.setAuthor(newspaper.getStaff().getName() + " (" + newspaper.getStaff().getEmail() + ")");
        newsResponse.setUpdateTime(newspaper.getTimeCreated().getTime());
        return newsResponse;
    }
}
