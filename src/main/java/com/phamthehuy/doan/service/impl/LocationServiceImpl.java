package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.City;
import com.phamthehuy.doan.entity.District;
import com.phamthehuy.doan.entity.Ward;
import com.phamthehuy.doan.repository.CityRepository;
import com.phamthehuy.doan.repository.DistrictRepository;
import com.phamthehuy.doan.repository.WardRepository;
import com.phamthehuy.doan.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private WardRepository wardRepository;

    @Override
    public List<City> getCities() throws Exception {
        return cityRepository.findAll();
    }

    @Override
    public List<District> getDistricts(Integer cityId) throws Exception {
//        cityId != null && cityId > 0 ? districtRepository.findDistrictsByCity_CityId(cityId) :
        return cityId != null && cityId > 0 ? districtRepository.findDistrictsByCity_CityId(cityId) : districtRepository.findAll();
    }

    @Override
    public List<Ward> getWards(Integer districtId) throws Exception {
//        districtId != null && districtId > 0 ? wardRepository.findWardsByDistrict_DistrictId(districtId) :
        return districtId != null && districtId > 0 ? wardRepository.findWardsByDistrict_DistrictId(districtId) : wardRepository.findAll();
    }
}
