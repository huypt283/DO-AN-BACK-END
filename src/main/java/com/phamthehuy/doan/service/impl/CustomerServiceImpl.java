package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.CustomerUpdateRequest;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.service.CustomerService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {
    final
    CustomerRepository customerRepository;

    final
    PasswordEncoder passwordEncoder;

    final
    StaffRepository staffRepository;

    public CustomerServiceImpl(StaffRepository staffRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.staffRepository = staffRepository;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<CustomerResponse> listCustomer(String search, Boolean deleted, String nameSort,
                                               String balanceSort, Integer page, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";
        String sort = null;
        String sortBy = null;
        if (balanceSort != null && !balanceSort.trim().equals("")) {
            sort = balanceSort;
            sortBy = "accountBalance";
        } else if (nameSort != null && !nameSort.trim().equals("")) {
            sort = nameSort;
            sortBy = "name";
        }
        Page<Customer> customerPage;
        if (sort == null) {
            if (deleted != null) {
                if (deleted)
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
                else
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
            } else
                customerPage = customerRepository.
                        findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
                                "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                PageRequest.of(page, limit)
                        );
        } else {
            if (sort.equalsIgnoreCase("desc")) {
                if (deleted != null) {
                    if (deleted)
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).descending())
                                );
                    else
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).descending())
                                );
                } else
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by(sortBy).descending())
                            );
            } else {
                if (deleted != null) {
                    if (deleted)
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedTrueOrPhoneLikeAndEnabledTrueAndDeletedTrueOrEmailLikeAndEnabledTrueAndDeletedTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).ascending())
                                );
                    else
                        customerPage = customerRepository.
                                findByNameLikeAndEnabledTrueAndDeletedFalseOrPhoneLikeAndEnabledTrueAndDeletedFalseOrEmailLikeAndEnabledTrueAndDeletedFalse(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by(sortBy).ascending())
                                );
                } else
                    customerPage = customerRepository.
                            findByNameLikeAndEnabledTrueOrPhoneLikeAndEnabledTrueOrEmailLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by(sortBy).ascending())
                            );
            }

        }

        List<Customer> customerList = customerPage.toList();

        //convert sang CustomerOutputDTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<CustomerResponse> customerResponseList = new ArrayList<>();
        for (Customer customer : customerList) {
            CustomerResponse customerResponse = modelMapper.map(customer, CustomerResponse.class);
            if (customer.getDob() != null) customerResponse.setBirthday(customer.getDob().getTime());
            customerResponseList.add(customerResponse);
        }
        return customerResponseList;
    }

    @Override
    public ResponseEntity<?> updateCustomer(CustomerUpdateRequest customerUpdateRequest,
                                            Integer id) throws BadRequestException {
        //validate
        String matchNumber = "[0-9]+";
        if (customerUpdateRequest.getCardId() != null && !customerUpdateRequest.getCardId().equals("")) {
            if (!customerUpdateRequest.getCardId().matches(matchNumber))
                throw new BadRequestException("Số CMND phải là số");
            else if (customerUpdateRequest.getCardId().length() < 9 || customerUpdateRequest.getCardId().length() > 12)
                throw new BadRequestException("Số CMND phải gồm 9-12 số");
        }
        if (!customerUpdateRequest.getPhone().matches(matchNumber))
            throw new BadRequestException("Số điện thoại phải là số");
        if (customerUpdateRequest.getBirthday() >= System.currentTimeMillis())
            throw new BadRequestException("Ngày sinh phải trong quá khứ");

        //update
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Optional<Customer> optionalCustomer = customerRepository.findById(id);
            Customer customer = optionalCustomer.get();
            customer.setName(customerUpdateRequest.getName());
            customer.setGender(customerUpdateRequest.isGender());
            customer.setAddress(customerUpdateRequest.getAddress());
            customer.setPhone(customerUpdateRequest.getPhone());
            customer.setCardId(customerUpdateRequest.getCardId());
            customer.setDob(new Date(customerUpdateRequest.getBirthday()));
            customer.setImage(customerUpdateRequest.getImage());
            Customer newCustomer = customerRepository.save(customer);
            CustomerResponse customerResponse = modelMapper.map(newCustomer, CustomerResponse.class);
            if (customer.getDob() != null) customerResponse.setBirthday(newCustomer.getDob().getTime());
            return ResponseEntity.ok(customerResponse);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new BadRequestException("Cập nhật khách hàng thất bại");
        }
    }

    @Override
    public MessageResponse blockCustomer(Integer id) throws BadRequestException {
        Customer customer = customerRepository.findByCustomerIdAndDeletedFalseAndEnabledTrue(id);
        if (customer == null) throw new BadRequestException("Lỗi: id " + id + " không tồn tại, hoặc đã block rồi");
        else {
            customer.setDeleted(true);
            customerRepository.save(customer);
            return new MessageResponse("Block khách hàng id " + id + " thành công");
        }
    }

    @Override
    public MessageResponse activeCustomer(Integer id) throws BadRequestException {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (!optionalCustomer.isPresent()) throw new BadRequestException("Lỗi: id " + id + " không tồn tại");
        else {
            optionalCustomer.get().setDeleted(false);
            customerRepository.save(optionalCustomer.get());
            return new MessageResponse("Kích hoạt khách hàng id: " + id + " thành công");
        }
    }

    @Override
    public ResponseEntity<?> findOneCustomer(Integer id) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Customer customer = customerRepository.findById(id).get();
            CustomerResponse customerResponse = modelMapper.map(customer, CustomerResponse.class);
            if (customer.getDob() != null) customerResponse.setBirthday(customer.getDob().getTime());
            return ResponseEntity.ok(customerResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi: khách hàng id " + id + " không tồn tại"));
        }
    }

    @Override
    public MessageResponse deleteAllCustomers() {
        List<Customer> customerList = customerRepository.findByDeletedTrueAndEnabledTrue();
        for (Customer customer : customerList) {
            customerRepository.delete(customer);
        }
        return new MessageResponse("Xóa tất cả khách hàng bị xóa mềm thành công");
    }

    @Override
    public MessageResponse deleteCustomers(Integer id) throws BadRequestException {
        Customer customer = customerRepository.findByCustomerIdAndEnabledTrue(id);
        if (customer == null) throw new BadRequestException("Khách hàng với id " + id + " không tồn tại");
        customerRepository.delete(customer);
        return new MessageResponse("Xóa hách hàng id " + id + " thành công");
    }
}
