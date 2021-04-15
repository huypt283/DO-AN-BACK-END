package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.StaffInsertRequest;
import com.phamthehuy.doan.model.request.StaffUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.service.StaffService;
import com.phamthehuy.doan.util.MailSender;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class StaffServiceImpl implements StaffService {
    final
    StaffRepository staffRepository;

    final
    PasswordEncoder passwordEncoder;

    final
    CustomerRepository customerRepository;

    final
    MailSender mailSender;

    final
    Helper helper;

    public StaffServiceImpl(StaffRepository staffRepository, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, MailSender mailSender, Helper helper) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.mailSender = mailSender;
        this.helper = helper;
    }

    @Override
    public List<StaffResponse> listStaff(String search, Boolean status, String sort,
                                         Integer page, Integer limit) {
        if (search == null || search.trim().equals("")) search = "";
        Page<Staff> staffPage;
        if (sort == null || sort.equals("")) {
            if (status != null) {
                if (status)
                    staffPage = staffRepository.
                            findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
                else
                    staffPage = staffRepository.
                            findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit)
                            );
            } else
                staffPage = staffRepository.
                        findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
                                "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                PageRequest.of(page, limit)
                        );
        } else {
            if (sort.equalsIgnoreCase("desc")) {
                if (status != null) {
                    if (status)
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").descending())
                                );
                    else
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").descending())
                                );
                } else
                    staffPage = staffRepository.
                            findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by("name").descending())
                            );
            } else {
                if (status != null) {
                    if (status)
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").ascending())
                                );
                    else
                        staffPage = staffRepository.
                                findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                        PageRequest.of(page, limit, Sort.by("name").ascending())
                                );
                } else
                    staffPage = staffRepository.
                            findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
                                    PageRequest.of(page, limit, Sort.by("name").ascending())
                            );
            }

        }

        List<Staff> staffList = staffPage.toList();

        //convert sang StaffOutputDTO
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        List<StaffResponse> staffResponseList = new ArrayList<>();
        for (Staff staff : staffList) {
            StaffResponse staffResponse = modelMapper.map(staff, StaffResponse.class);
            staffResponse.setBirthday(staff.getDob());
            staffResponseList.add(staffResponse);
        }
        return staffResponseList;
    }

    @Override
    public MessageResponse insertStaff(StaffInsertRequest staffInsertRequest, HttpServletRequest request) throws Exception {
        //validation
        if (customerRepository.findByEmail(staffInsertRequest.getEmail()) != null)
            throw new BadRequestException("Email đã khách hàng sử dụng");
        if (staffRepository.findByEmail(staffInsertRequest.getEmail()) != null)
            throw new BadRequestException("Email đã được nhân viên sử dụng");
        String matchNumber = "[0-9]+";
        if (!staffInsertRequest.getCardId().matches(matchNumber))
            throw new BadRequestException("Số CMND phải là số");
        if (!staffInsertRequest.getPhone().matches(matchNumber))
            throw new BadRequestException("Số điện thoại phải là số");
        if (staffInsertRequest.getBirthday() >= System.currentTimeMillis())
            throw new BadRequestException("Ngày sinh phải trong quá khứ");

        //create token
        String token = helper.createUserToken(30);

        //insert
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Staff staff = modelMapper.map(staffInsertRequest, Staff.class);
            staff.setDob(new Date((staffInsertRequest.getBirthday())));
            staff.setPass(passwordEncoder.encode(staffInsertRequest.getPass()));
            staff.setToken(token);
            Staff newStaff = staffRepository.save(staff);
            //StaffOutputDTO staffOutputDTO = modelMapper.map(newStaff, StaffOutputDTO.class);
            //staffOutputDTO.setBirthday(newStaff.getDob().getTime());

            //send mail
            mailSender.send(
                    staffInsertRequest.getEmail(),
                    "Xác nhận địa chỉ email",
                    "Click vào đường link sau để xác nhận email và kích hoạt tài khoản của bạn:<br/>" +
                            helper.getHostUrl(request.getRequestURL().toString(), "/super-admin") + "/confirm?token-customer=" + token
                            + "&email=" + staffInsertRequest.getEmail(),
                    "Thời hạn xác nhận email: 10 phút kể từ khi đăng kí"
            );

            Thread deleteDisabledStaff = new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    Thread.sleep(10*60*1000);
                    Optional<Staff> optionalStaff=
                            staffRepository.findByStaffIdAndEnabledFalse(newStaff.getStaffId());
                    if(optionalStaff.isPresent())
                        staffRepository.delete(optionalStaff.get());
                }
            };
            deleteDisabledStaff.start();

            return new MessageResponse("Bạn hãy check mail để xác nhận, thời hạn 10 phút kể từ khi đăng kí");
        } catch (Exception e) {
            //e.printStackTrace();
            throw new BadRequestException("Thêm mới nhân viên thất bại");
        }
    }

    @Override
    public ResponseEntity<?> updateStaff(StaffUpdateRequest staffUpdateRequest, Integer id) throws BadRequestException {
        //validate
        String matchNumber = "[0-9]+";
        if (!staffUpdateRequest.getCardId().matches(matchNumber))
            throw new BadRequestException("Số CMND phải là số");
        if (!staffUpdateRequest.getPhone().matches(matchNumber))
            throw new BadRequestException("Số điện thoại phải là số");
        if (staffUpdateRequest.getBirthday() >= System.currentTimeMillis())
            throw new BadRequestException("Ngày sinh phải trong quá khứ");

        //update
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Optional<Staff> optionalStaff = staffRepository.findById(id);
            Staff staff = optionalStaff.get();
            staff.setName(staffUpdateRequest.getName());
            staff.setCardId(staffUpdateRequest.getCardId());
            staff.setDob(new Date(staffUpdateRequest.getBirthday()));
            staff.setGender(staffUpdateRequest.isGender());
            staff.setRole(staffUpdateRequest.isRole());
            staff.setAddress(staffUpdateRequest.getAddress());
            staff.setPhone(staffUpdateRequest.getPhone());
            staff.setImage(staffUpdateRequest.getImage());
            Staff newStaff = staffRepository.save(staff);
            StaffResponse staffResponse = modelMapper.map(newStaff, StaffResponse.class);
            staffResponse.setBirthday(newStaff.getDob());
            return ResponseEntity.ok(staffResponse);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new BadRequestException("Cập nhật nhân viên thất bại");
        }
    }

    @Override
    public MessageResponse blockStaff(Integer id) throws BadRequestException {
        Staff staff = staffRepository.findByStaffIdAndDeletedFalseAndEnabledTrue(id);
        if (staff == null) throw new BadRequestException("Lỗi: id " + id + " không tồn tại");
        else {
            staff.setDeleted(true);
            staffRepository.save(staff);
            return new MessageResponse("Block nhân viên id " + id + " thành công");
        }
    }

    @Override
    public MessageResponse activeStaff(Integer id) throws BadRequestException {
        Optional<Staff> optionalStaff = staffRepository.findById(id);
        if (!optionalStaff.isPresent()) throw new BadRequestException("Lỗi: id " + id + " không tồn tại");
        else {
            optionalStaff.get().setDeleted(false);
            staffRepository.save(optionalStaff.get());
            return new MessageResponse("Kích hoạt nhân viên id: " + id + " thành công");
        }
    }

    @Override
    public ResponseEntity<?> findOneStaff(Integer id) {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Staff newStaff = staffRepository.findById(id).get();
            StaffResponse staffResponse = modelMapper.map(newStaff, StaffResponse.class);
            staffResponse.setBirthday(newStaff.getDob());
            return ResponseEntity.ok(staffResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Lỗi: nhân viên id " + id + " không tồn tại"));
        }
    }

    @Override
    public MessageResponse deleteAllStaffs() {
        List<Staff> staffList = staffRepository.findByDeletedTrueAndEnabledTrue();
        for (Staff staff : staffList) {
            staffRepository.delete(staff);
        }
        return new MessageResponse("Xóa tất cả nhân viên bị xóa mềm thành công");
    }

    @Override
    public MessageResponse deleteStaffs(Integer id) throws BadRequestException {
        Staff staff=staffRepository.
                findByEnabledTrueAndStaffId(id);
        if(staff==null) throw new BadRequestException("Nhân viên id: "+id+" không tồn tại");
        staffRepository.delete(staff);
        return new MessageResponse("Xóa nhân viên "+id+" thành công");
    }
}
