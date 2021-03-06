package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.exception.AccessDeniedException;
import com.phamthehuy.doan.exception.ConflictException;
import com.phamthehuy.doan.exception.InternalServerError;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.StaffInsertRequest;
import com.phamthehuy.doan.model.request.StaffUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.StaffService;
import com.phamthehuy.doan.util.MailSender;
import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StaffServiceImpl implements StaffService {
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private Helper helper;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public List<StaffResponse> listAllStaff() {
        List<Staff> staffs = staffRepository.findAll();
        return staffs.stream().filter(staff -> staff.getStaffId() != 1).map(this::convertToStaffResponse).collect(Collectors.toList());
    }

    @Override
    public StaffResponse findStaffById(Integer id) throws Exception {
        if (id == 1) {
            throw new NotFoundException("Không tìm thấy tài khoản này");
        }

        Staff staff = staffRepository.findByStaffId(id);
        validateStaff(staff);

        return this.convertToStaffResponse(staff);
    }

    @Override
    public MessageResponse insertStaff(StaffInsertRequest staffInsertRequest) throws Exception {
        //validation
        String email = staffInsertRequest.getEmail();
        if (customerRepository.findByEmail(email) != null || staffRepository.findByEmail(email) != null)
            throw new ConflictException("Email này đã được sử dụng");

        String token = helper.createUserToken(30);

        //insert
        try {
            Staff staff = new Staff();
            BeanUtils.copyProperties(staffInsertRequest, staff);
            staff.setDob(staffInsertRequest.getBirthday());
            staff.setPass(passwordEncoder.encode(staffInsertRequest.getPass()));
            staff.setToken(token);

            staffRepository.save(staff);

            //send mail
            mailSender.send(
                    staffInsertRequest.getEmail(),
                    "Xác nhận địa chỉ email",
                    "Click vào đường link sau để xác nhận email và kích hoạt tài khoản của bạn:<br/>" +
                            this.clientUrl + "/confirm?token=" + token
                            + "&email=" + staffInsertRequest.getEmail(),
                    "Thời hạn xác nhận email: 20 phút kể từ khi được tạo tài khoản"
            );

            Thread deleteUnconfirmed = new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    Thread.sleep(20 * 60 * 1000);
                    Staff staff = staffRepository.findByToken(token);
                    if (staff != null)
                        staffRepository.delete(staff);
                }
            };
            deleteUnconfirmed.start();

            return new MessageResponse("Tạo tài khoản thành công, bạn hãy thông báo cho nhân viên xác nhận email trong 20 phút từ khi tạo mới tài khoản");
        } catch (Exception e) {
            throw new InternalServerError("Thêm mới nhân viên thất bại");
        }
    }

    @Override
    public MessageResponse updateStaffById(Integer id, StaffUpdateRequest staffUpdateRequest) throws Exception {
        Staff staff = staffRepository.findByStaffId(id);
        validateStaff(staff);

        if (staff.getStaffId() == 1) {
            throw new AccessDeniedException("Không được cập nhật tài khoản này");
        }

        BeanUtils.copyProperties(staffUpdateRequest, staff);
        staff.setDob(staffUpdateRequest.getBirthday());

        staff = staffRepository.save(staff);

        return new MessageResponse("Cập nhật thông tin nhân viên thành công");
    }

    @Override
    public MessageResponse activeStaffById(Integer id) throws Exception {
        Staff staff = staffRepository.findByStaffId(id);
        validateStaff(staff);
        if (BooleanUtils.isFalse(staff.getDeleted()))
            throw new ConflictException("Nhân viên này không bị khoá");

        if (staff.getStaffId() == 1) {
            throw new AccessDeniedException("Không được cập nhật tài khoản này");
        }

        staff.setDeleted(false);
        staffRepository.save(staff);
        return new MessageResponse("Mở khoá nhân viên thành công");
    }

    @Override
    public MessageResponse blockStaffById(Integer id) throws Exception {
        Staff staff = staffRepository.findByStaffId(id);
        validateStaff(staff);
        if (BooleanUtils.isTrue(staff.getDeleted()))
            throw new ConflictException("Nhân viên này đã bị khoá");

        if (staff.getStaffId() == 1) {
            throw new AccessDeniedException("Không được cập nhật tài khoản này");
        }

        staff.setDeleted(true);
        staffRepository.save(staff);
        return new MessageResponse("Khoá nhân viên thành công");
    }

    @Override
    public MessageResponse deleteStaffById(Integer id) throws Exception {
        Staff staff = staffRepository.findByStaffId(id);
        validateStaff(staff);

        if (staff.getStaffId() == 1) {
            throw new AccessDeniedException("Không được xoá tài khoản này");
        }

        staffRepository.delete(staff);
        return new MessageResponse("Xóa nhân viên thành công");
    }

    private StaffResponse convertToStaffResponse(Staff staff) {
        StaffResponse staffResponse = new StaffResponse();
        BeanUtils.copyProperties(staff, staffResponse);
        staffResponse.setBirthday(staff.getDob());
        staffResponse.setRole(staff.getRole() ? "SUPER_ADMIN" : "ADMIN");
        return staffResponse;
    }

    private void validateStaff(Staff staff) {
        if (staff == null)
            throw new NotFoundException("Không tìm thấy nhân viên");
    }

    //    @Override
//    public List<StaffResponse> listStaff(String search, Boolean status, String sort,
//                                         Integer page, Integer limit) {
//        if (search == null || search.trim().equals("")) search = "";
//        Page<Staff> staffPage;
//        if (sort == null || sort.equals("")) {
//            if (status != null) {
//                if (status)
//                    staffPage = staffRepository.
//                            findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit)
//                            );
//                else
//                    staffPage = staffRepository.
//                            findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit)
//                            );
//            } else
//                staffPage = staffRepository.
//                        findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
//                                "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                PageRequest.of(page, limit)
//                        );
//        } else {
//            if (sort.equalsIgnoreCase("desc")) {
//                if (status != null) {
//                    if (status)
//                        staffPage = staffRepository.
//                                findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by("name").descending())
//                                );
//                    else
//                        staffPage = staffRepository.
//                                findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by("name").descending())
//                                );
//                } else
//                    staffPage = staffRepository.
//                            findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit, Sort.by("name").descending())
//                            );
//            } else {
//                if (status != null) {
//                    if (status)
//                        staffPage = staffRepository.
//                                findByNameLikeAndDeletedTrueAndEnabledTrueOrEmailLikeAndDeletedTrueAndEnabledTrueOrPhoneLikeAndDeletedTrueAndEnabledTrue(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by("name").ascending())
//                                );
//                    else
//                        staffPage = staffRepository.
//                                findByNameLikeAndDeletedFalseAndEnabledTrueOrEmailLikeAndDeletedFalseAndEnabledTrueOrPhoneLikeAndDeletedFalseAndEnabledTrue(
//                                        "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                        PageRequest.of(page, limit, Sort.by("name").ascending())
//                                );
//                } else
//                    staffPage = staffRepository.
//                            findByNameLikeAndEnabledTrueOrEmailLikeAndEnabledTrueOrPhoneLikeAndEnabledTrue(
//                                    "%" + search + "%", "%" + search + "%", "%" + search + "%",
//                                    PageRequest.of(page, limit, Sort.by("name").ascending())
//                            );
//            }
//
//        }
//
//        List<Staff> staffList = staffPage.toList();
//
//        return staffs.stream().map(this::convertToStaffResponse).collect(Collectors.toList());
//    }
}
