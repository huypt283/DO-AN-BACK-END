package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.CustomerUpdateRequest;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CustomerService {
    //Tìm kiếm khách hàng = email, phone, name
    //sắp xếp theo name, balance =asc/desc (chỉ truyền 1 trong 2)
    //phân trang
    //nếu không truyền vào tham số thì trả về all list
    List<CustomerResponse> listCustomer(String search, Boolean deleted, String nameSort,
                                        String balanceSort, Integer page, Integer limit);

    //    cập nhật thông tin khách hàng	Put/super-admin/customers
    ResponseEntity<?> updateCustomer(CustomerUpdateRequest customerUpdateRequest,
                                     Integer id) throws BadRequestException;

    //    block khách hàng	DELETE/super-admin/customers/{id}
    MessageResponse blockCustomer(Integer id) throws BadRequestException;

    //    active khách hàng
    MessageResponse activeCustomer(Integer id) throws BadRequestException;

    //    xem thông tin khách hàng	GET/super-admin/customers/{id}
    ResponseEntity<?> findOneCustomer(Integer id);

    //xóa cứng tất cả customer bị xóa mềm
    MessageResponse deleteAllCustomers();

    //xóa cứng 1 list (mảng Integer Id) khách hàng bị xóa mềm
    MessageResponse deleteCustomers(Integer id) throws BadRequestException;
}
