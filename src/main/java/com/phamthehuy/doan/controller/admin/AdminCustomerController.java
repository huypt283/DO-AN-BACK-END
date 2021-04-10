package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.CustomerUpdateRequest;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/customers")
public class AdminCustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<?> listCustomers(@RequestParam(required = false, defaultValue = "1") Integer page,
                                                @RequestParam(required = false, defaultValue = "10") Integer limit) throws Exception {
        return new ResponseEntity<>(customerService.listCustomer(page, limit), HttpStatus.OK);
    }

    //cập nhật thông tin nhân viên	POST/admin/customers
    @PostMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@Valid @RequestBody CustomerUpdateRequest customerUpdateRequest,
                                            @PathVariable Integer id)
            throws BadRequestException {
        return customerService.updateCustomer(customerUpdateRequest, id);
    }

    //    block nhân viên	GET/admin/customers/block/{id}
    @GetMapping("/block/{id}")
    public MessageResponse blockCustomer(@PathVariable Integer id) throws BadRequestException {
        return customerService.blockCustomer(id);
    }

    //    active nhân viên	DELETE/admin/customers/block/{id}
    @GetMapping("/active/{id}")
    public MessageResponse activeCustomer(@PathVariable Integer id) throws BadRequestException {
        return customerService.activeCustomer(id);
    }

    //    xem thông tin nhân viên	GET/admin/customers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> findOneCustomer(@PathVariable Integer id) {
        return customerService.findOneCustomer(id);
    }

    @DeleteMapping("/{id}")
    public MessageResponse deleteCustomers(@PathVariable Integer id) throws BadRequestException {
        return customerService.deleteCustomers(id);
    }
}
