package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.model.request.CustomerUpdateRequest;
import com.phamthehuy.doan.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/customers")
public class AdminCustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<?> listAllCustomer(@RequestParam(required = false, defaultValue = "1") Integer page,
                                             @RequestParam(required = false, defaultValue = "10") Integer limit) throws Exception {
        return new ResponseEntity<>(customerService.listCustomer(page, limit), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detailCustomer(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(customerService.findCustomerById(id), HttpStatus.OK);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateCustomer(@PathVariable Integer id,
//                                            @Valid @RequestBody CustomerUpdateRequest customerUpdateRequest) throws Exception {
//        return new ResponseEntity<>(customerService.updateCustomerById(customerUpdateRequest, id), HttpStatus.OK);
//    }

    @PostMapping("/active/{id}")
    public ResponseEntity<?> activeCustomer(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(customerService.activeCustomerById(id), HttpStatus.OK);
    }

    @PostMapping("/block/{id}")
    public ResponseEntity<?> blockCustomer(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(customerService.blockCustomerById(id), HttpStatus.OK);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteCustomer(@PathVariable Integer id) throws Exception {
//        return new ResponseEntity<>(customerService.deleteCustomerById(id), HttpStatus.OK);
//    }
}
