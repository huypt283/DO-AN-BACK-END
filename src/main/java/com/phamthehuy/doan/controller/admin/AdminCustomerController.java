package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.CustomerUpdateRequest;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/customers")
public class AdminCustomerController {
    final
    CustomerService customerService;

    public AdminCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    //sắp xếp theo name, accountBalance (chỉ chọn 1 trong 2)
    @GetMapping
    public List<CustomerResponse> listCustomers
    (@RequestParam(required = false) String search,
     @RequestParam(required = false) Boolean deleted,
     @RequestParam(value = "name-sort", required = false) String nameSort,
     @RequestParam(value = "balance-sort", required = false) String balanceSort,
     @RequestParam Integer page,
     @RequestParam Integer limit) {
        return customerService.listCustomer(search, deleted, nameSort, balanceSort, page, limit);
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
