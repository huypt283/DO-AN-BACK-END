package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.request.StaffInsertRequest;
import com.phamthehuy.doan.model.request.StaffUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;

import java.util.List;

public interface StaffService {
    List<StaffResponse> listAllStaff();

    StaffResponse findStaffById(Integer id) throws Exception;

    MessageResponse insertStaff(StaffInsertRequest staffInsertRequest) throws Exception;

    StaffResponse updateStaffById(Integer id, StaffUpdateRequest staffUpdateRequest) throws Exception;

    MessageResponse activeStaffById(Integer id) throws Exception;

    MessageResponse blockStaffById(Integer id) throws Exception;

    MessageResponse deleteStaffById(Integer id) throws Exception;
}
