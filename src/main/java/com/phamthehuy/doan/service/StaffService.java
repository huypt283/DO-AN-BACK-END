package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.StaffInsertRequest;
import com.phamthehuy.doan.model.request.StaffUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface StaffService {

    //Tìm kiếm nhân viên = email, phone, name
    //sắp xếp theo name sort=asc, sort=desc
    //phân trang
    //nếu không truyền vào tham số thì trả về all list
    List<StaffResponse> listStaff(String search,
                                  Boolean status, String sort,
                                  Integer page,
                                  Integer limit);

    //    thêm nhân viên	Post/super-admin/staffs
    MessageResponse insertStaff(StaffInsertRequest staffInsertRequest, HttpServletRequest request) throws Exception;

    //    cập nhật thông tin nhân viên	Put/super-admin/staffs
    ResponseEntity<?> updateStaff(StaffUpdateRequest staffUpdateRequest, Integer id) throws CustomException;

    //    block nhân viên	DELETE/super-admin/staffs/{id}
    MessageResponse blockStaff(Integer id) throws CustomException;

    //    active nhân viên
    MessageResponse activeStaff(Integer id) throws CustomException;

    //    xem thông tin nhân viên	GET/super-admin/staffs/{id}
    ResponseEntity<?> findOneStaff(Integer id);

    //xóa cứng tất cả staff bị xóa mềm
    MessageResponse deleteAllStaffs();

    //xóa cứng 1 list (mảng Integer Id) nhân viên bị xóa mềm
    MessageResponse deleteStaffs(Integer id) throws CustomException;
}
