package com.phamthehuy.doan.repository;

import com.phamthehuy.doan.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findDistrictsByCity_CityId(Integer cityId);
}
