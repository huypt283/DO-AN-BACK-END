package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.request.CustomerUpdateRequest;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;

import java.util.List;

public interface CustomerService {
    List<CustomerResponse> listCustomer(Integer page, Integer limit) throws Exception;

    CustomerResponse findCustomerById(Integer id) throws Exception;

    CustomerResponse updateCustomerById(CustomerUpdateRequest customerUpdateRequest,
                                        Integer id) throws Exception;

    MessageResponse activeCustomerById(Integer id) throws Exception;

    MessageResponse blockCustomerById(Integer id) throws Exception;

    MessageResponse deleteCustomerById(Integer id) throws Exception;

    MessageResponse deleteAllBlockCustomers() throws Exception;


}
