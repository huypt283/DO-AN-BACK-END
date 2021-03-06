package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Integer> {
    List<Ward> findWardsByDistrict_DistrictId(Integer districtId);
}
