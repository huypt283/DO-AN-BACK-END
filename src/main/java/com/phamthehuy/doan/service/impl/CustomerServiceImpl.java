package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.dao.CustomerRepository;
import com.phamthehuy.doan.dao.StaffRepository;
import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.dto.input.CustomerUpdateDTO;
import com.phamthehuy.doan.model.dto.output.CustomerOutputDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.entity.Customer;
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
    public List<CustomerOutputDTO> listCustomer(String search, Boolean deleted, String nameSort,
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
        List<CustomerOutputDTO> customerOutputDTOList = new ArrayList<>();
        for (Customer customer : customerList) {
            CustomerOutputDTO customerOutputDTO = modelMapper.map(customer, CustomerOutputDTO.class);
            if (customer.getDob() != null) customerOutputDTO.setBirthday(customer.getDob().getTime());
            customerOutputDTOList.add(customerOutputDTO);
        }
        return customerOutputDTOList;
    }

    @Override
    public ResponseEntity<?> updateCustomer(CustomerUpdateDTO customerUpdateDTO,
                                            Integer id) throws CustomException {
        //validate
        String matchNumber = "[0-9]+";
        if (customerUpdateDTO.getCardId() != null && !customerUpdateDTO.getCardId().equals("")) {
            if (!customerUpdateDTO.getCardId().matches(matchNumber))
                throw new CustomException("Số CMND phải là số");
            else if (customerUpdateDTO.getCardId().length() < 9 || customerUpdateDTO.getCardId().length() > 12)
                throw new CustomException("Số CMND phải gồm 9-12 số");
        }
        if (!customerUpdateDTO.getPhone().matches(matchNumber))
            throw new CustomException("Số điện thoại phải là số");
        if (customerUpdateDTO.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ngày sinh phải trong quá khứ");

        //update
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Optional<Customer> optionalCustomer = customerRepository.findById(id);
            Customer customer = optionalCustomer.get();
            customer.setName(customerUpdateDTO.getName());
            customer.setGender(customerUpdateDTO.isGender());
            customer.setAddress(customerUpdateDTO.getAddress());
            customer.setPhone(customerUpdateDTO.getPhone());
            customer.setCardId(customerUpdateDTO.getCardId());
            customer.setDob(new Date(customerUpdateDTO.getBirthday()));
            customer.setImage(customerUpdateDTO.getImage());
            Customer newCustomer = customerRepository.save(customer);
            CustomerOutputDTO customerOutputDTO = modelMapper.map(newCustomer, CustomerOutputDTO.class);
            if (customer.getDob() != null) customerOutputDTO.setBirthday(newCustomer.getDob().getTime());
            return ResponseEntity.ok(customerOutputDTO);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Cập nhật khách hàng thất bại");
        }
    }

    @Override
    public Message blockCustomer(Integer id) throws CustomException {
        Customer customer = customerRepository.findByCustomerIdAndDeletedFalseAndEnabledTrue(id);
        if (customer == null) throw new CustomException("Lỗi: id " + id + " không tồn tại, hoặc đã block rồi");
        else {
            customer.setDeleted(true);
            customerRepository.save(customer);
            return new Message("Block khách hàng id " + id + " thành công");
        }
    }

    @Override
    public Message activeCustomer(Integer id) throws CustomException {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (!optionalCustomer.isPresent()) throw new CustomException("Lỗi: id " + id + " không tồn tại");
        else {
            optionalCustomer.get().setDeleted(false);
            customerRepository.save(optionalCustomer.get());
            return new Message("Kích hoạt khách hàng id: " + id + " thành công");
        }
    }

    @Override
    public ResponseEntity<?> findOneCustomer(Integer id) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Customer customer = customerRepository.findById(id).get();
            CustomerOutputDTO customerOutputDTO = modelMapper.map(customer, CustomerOutputDTO.class);
            if (customer.getDob() != null) customerOutputDTO.setBirthday(customer.getDob().getTime());
            return ResponseEntity.ok(customerOutputDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Message("Lỗi: khách hàng id " + id + " không tồn tại"));
        }
    }

    @Override
    public Message deleteAllCustomers() {
        List<Customer> customerList = customerRepository.findByDeletedTrueAndEnabledTrue();
        for (Customer customer : customerList) {
            customerRepository.delete(customer);
        }
        return new Message("Xóa tất cả khách hàng bị xóa mềm thành công");
    }

    @Override
    public Message deleteCustomers(Integer id) throws CustomException {
        Customer customer = customerRepository.findByCustomerIdAndEnabledTrue(id);
        if (customer == null) throw new CustomException("Khách hàng với id " + id + " không tồn tại");
        customerRepository.delete(customer);
        return new Message("Xóa hách hàng id " + id + " thành công");
    }
}
