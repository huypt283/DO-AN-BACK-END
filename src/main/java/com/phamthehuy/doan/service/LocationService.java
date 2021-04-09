package com.phamthehuy.doan.service;

import com.phamthehuy.doan.entity.City;
import com.phamthehuy.doan.entity.District;
import com.phamthehuy.doan.entity.Ward;

import java.util.List;

public interface LocationService {
    List<City> getCities() throws Exception;

    List<District> getDistricts(Integer cityId) throws Exception;

    List<Ward> getWards(Integer districtId) throws Exception;
}
