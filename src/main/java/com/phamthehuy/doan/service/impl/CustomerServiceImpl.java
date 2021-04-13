package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.exception.ConflictException;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.model.request.CustomerUpdateRequest;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.CustomerService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    StaffRepository staffRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public List<CustomerResponse> listCustomer(Integer page, Integer limit) throws Exception {
//        OffsetBasedPageRequest pageable = new OffsetBasedPageRequest((page - 1) * limit, limit, Sort.by("id").descending();
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(this::convertToCustomerResponse).collect(Collectors.toList());
    }

    @Override
    public CustomerResponse findCustomerById(Integer id) throws Exception {
        Customer customer = customerRepository.findByCustomerId(id);
        if (customer == null) {
            throw new NotFoundException("Không tìm thấy khách hàng");
        }

        CustomerResponse customerResponse = new CustomerResponse();
        BeanUtils.copyProperties(customer, customerResponse);
        customerResponse.setBirthday(customer.getDob());

        return customerResponse;
    }

    @Override
    public CustomerResponse updateCustomerById(CustomerUpdateRequest customerUpdateRequest,
                                               Integer id) throws Exception {
        Customer customer = customerRepository.findByCustomerId(id);
        if (customer == null) {
            throw new NotFoundException("Không tìm thấy khách hàng");
        }

        BeanUtils.copyProperties(customerUpdateRequest, customer);
        customer.setDob(customerUpdateRequest.getBirthday());

        customer = customerRepository.save(customer);

        CustomerResponse customerResponse = new CustomerResponse();
        BeanUtils.copyProperties(customer, customerResponse);
        customerResponse.setBirthday(customer.getDob());

        return customerResponse;
    }

    private CustomerResponse convertToCustomerResponse(Customer customer) {
        CustomerResponse customerResponse = new CustomerResponse();
        BeanUtils.copyProperties(customer, customerResponse);
        customerResponse.setBirthday(customer.getDob());
        return customerResponse;
    }

    @Override
    public MessageResponse activeCustomerById(Integer id) throws Exception {
        Customer customer = customerRepository.findByCustomerId(id);
        if (customer == null) {
            throw new NotFoundException("Không tìm thấy khách hàng");
        } else {
            if (BooleanUtils.isFalse(customer.getDeleted()))
                throw new ConflictException("Khách hàng này không bị khoá");

            customer.setDeleted(false);
            customerRepository.save(customer);
            return new MessageResponse("Kích hoạt khách hàng thành công");
        }
    }

    @Override
    public MessageResponse blockCustomerById(Integer id) throws Exception {
        Customer customer = customerRepository.findByCustomerId(id);
        if (customer == null) {
            throw new NotFoundException("Không tìm thấy khách hàng");
        } else {
            if (BooleanUtils.isTrue(customer.getDeleted()))
                throw new ConflictException("Khách hàng này đã bị khoá");

            customer.setDeleted(true);
            customerRepository.save(customer);
            return new MessageResponse("Khoá khách hàng thành công");
        }
    }

    @Override
    public MessageResponse deleteCustomerById(Integer id) throws BadRequestException {
        Customer customer = customerRepository.findByCustomerId(id);
        if (customer == null) {
            throw new NotFoundException("Không tìm thấy khách hàng");
        } else {
            customerRepository.delete(customer);
            return new MessageResponse("Xóa hách hàng thành công");
        }
    }

    @Override
    public MessageResponse deleteAllBlockCustomers() {
        List<Customer> customers = customerRepository.findByDeletedTrue();
        customers.forEach(customer -> customerRepository.delete(customer));

        return new MessageResponse("Xóa tất cả khách hàng bị khoá thành công");
    }

//    public List<CustomerResponse> listCustomer(String search, Boolean deleted, String nameSort,
//                                               String balanceSort, Integer page, Integer limit) {
//        if (search == null || search.trim().equals("")) search = "";
//        String sort = null;
//        String sortBy = null;
//        if (balanceSort != null && !balanceSort.trim().equals("")) {
//            sort = balanceSort;
//            sortBy = "accountBalance";
//        } else if (nameSort != null && !nameSort.trim().equals("")) {
//            sort = nameSort;
//            sortBy = "name";
//        }
//        Page<Customer> customerPage;
//        if (sort == null) {
//            if (deleted != null) {
//                if (deleted)
//                    customerPage = customerRepository.
//                            findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit)
//                            );
//                else
//                    customerPage = customerRepository.
//                            findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit)
//                            );
//            } else
//                customerPage = customerRepository.
//                        findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
//                                "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                PageRequest.of(page, limit)
//                        );
//        } else {
//            if (sort.equalsIgnoreCase("desc")) {
//                if (deleted != null) {
//                    if (deleted)
//                        customerPage = customerRepository.
//                                findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by(sortBy).descending())
//                                );
//                    else
//                        customerPage = customerRepository.
//                                findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by(sortBy).descending())
//                                );
//                } else
//                    customerPage = customerRepository.
//                            findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit, Sort.by(sortBy).descending())
//                            );
//            } else {
//                if (deleted != null) {
//                    if (deleted)
//                        customerPage = customerRepository.
//                                findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by(sortBy).ascending())
//                                );
//                    else
//                        customerPage = customerRepository.
//                                findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by(sortBy).ascending())
//                                );
//                } else
//                    customerPage = customerRepository.
//                            findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit, Sort.by(sortBy).ascending())
//                            );
//            }
//
//        }
//
//        List<Customer> customerList = customerPage.toList();
//
//        //convert sang CustomerOutputDTO
//        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.getConfiguration()
//                .setMatchingStrategy(MatchingStrategies.STRICT);
//        List<CustomerResponse> customerResponseList = new ArrayList<>();
//        for (Customer customer : customerList) {
//            CustomerResponse customerResponse = modelMapper.map(customer, CustomerResponse.class);
//            customerResponse.setBirthday(customer.getDob());
//            customerResponseList.add(customerResponse);
//        }
//        return customerResponseList;
//    }
}
