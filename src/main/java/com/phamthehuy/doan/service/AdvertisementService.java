package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.dto.input.AdvertisementInsertDTO;
import com.phamthehuy.doan.model.dto.input.AdvertisementUpdateDTO;
import com.phamthehuy.doan.model.dto.output.AdvertisementOutputDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AdvertisementService{
    //danh sách bài viêt
    List<AdvertisementOutputDTO> listAdvertisement(Integer page, Integer limit);

    //thêm bài viết
    ResponseEntity<?> insertAdvertisement(AdvertisementInsertDTO advertisementInsertDTO);

    //cập nhật bài viết
    ResponseEntity<?> updateAdvertisement(AdvertisementUpdateDTO advertisementUpdateDTO);

    //xóa bài viết
    ResponseEntity<String> deleteAdvertisement(Integer id);
//
//    //duyệt bài viết
//    ResponseEntity<String> activeArticle(Integer id);
//
//    //xem bài viết
//    ResponseEntity<?> findOneArticle(Integer id);

}
