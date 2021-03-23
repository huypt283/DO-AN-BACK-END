package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.dto.input.StaffInsertDTO;
import com.phamthehuy.doan.model.dto.input.StaffUpdateDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.dto.output.StaffOutputDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface StaffService {

    List<StaffOutputDTO> listStaff(Integer page, Integer limit);

    //    thêm nhân viên	Post/super-admin/staffs
    ResponseEntity<?> insertStaff(StaffInsertDTO staffInsertDTO);

    //    cập nhật thông tin nhân viên	Put/super-admin/staffs
    ResponseEntity<?> updateStaff(StaffUpdateDTO staffUpdateDTO);

    //    block nhân viên	DELETE/super-admin/staffs/{id}
    Message blockStaff(Integer id);

    //    active nhân viên
    Message activeStaff(Integer id);

    //    tìm kiếm nhân viên bằng email	hoặc sđt hoặc họ tên GET/super-admin/staffs?search={search}
    List<StaffOutputDTO> searchStaff(String search, Integer page, Integer limit);


    //    xem thông tin nhân viên	GET/super-admin/staffs/{id}
    ResponseEntity<?> findOneStaff(Integer id);

    //xóa cứng tất cả staff bị xóa mềm
    Message deleteStaffs();
}
