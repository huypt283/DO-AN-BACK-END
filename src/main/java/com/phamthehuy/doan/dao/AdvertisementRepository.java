package com.phamthehuy.doan.dao;

import com.phamthehuy.doan.model.entity.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Integer> {
    Page<Advertisement> findByDeletedFalse(Pageable pageable);

    List<Advertisement> findByDeletedFalse();

    Advertisement findByAdvertisementIdAndDeletedFalse(Integer advertisementId);

    List<Advertisement> findByTitleLike(String title);

}
