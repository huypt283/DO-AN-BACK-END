package com.phamthehuy.doan.controller.common;

import com.phamthehuy.doan.authentication.CustomUserDetailsService;
import com.phamthehuy.doan.authentication.JwtUtil;
import com.phamthehuy.doan.dao.StaffRepository;
import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.dto.input.*;
import com.phamthehuy.doan.model.dto.output.CustomerOutputDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.dto.output.StaffOutputDTO;
import com.phamthehuy.doan.service.AccountService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
public class AccountController {
    final
    AccountService accountService;

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsService userDetailsService;

    private final JwtUtil jwtTokenUtil;

    final
    StaffRepository staffRepository;

    public AccountController(AccountService accountService, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtTokenUtil, StaffRepository staffRepository) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.staffRepository = staffRepository;
    }


    @PostMapping("/sign-up")
    public Message customerSignup(@Valid @RequestBody SignupDTO signupDTO, HttpServletRequest request) throws CustomException {
        return accountService.customerSignup(signupDTO, request);
    }

    @GetMapping("/confirm")
    public Message confirmEmail(@RequestParam(value = "token-customer") String token,
                                @RequestParam String email) throws CustomException {
        return accountService.confirmEmail(token, email);
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginDTO loginDTO) throws Exception {
        //login danh cho khach hang ->role: customer
        //login danh cho nhan vien -> role: admin, super admin
        return accountService.login(loginDTO);
    }

    //Brear Token
    //isRefreshToken = true (Header)
    @GetMapping("/refreshtoken")
    public Map<String, String> refreshtoken(HttpServletRequest request) throws CustomException {
        return accountService.refreshtoken(request);
    }

    @GetMapping("/forgot")
    public Message forgotPassword(@RequestParam String email) throws CustomException {
        return accountService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public Message resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) throws CustomException {
        return accountService.resetPassword(resetPasswordDTO);
    }

    @GetMapping("/admin/profile")
    public StaffOutputDTO staffProfile(HttpServletRequest request)
            throws CustomException {
        return accountService.staffDetail(request);
    }

    @PostMapping("/admin/update-profile")
    public StaffOutputDTO staffUpdateProfile(@Valid @RequestBody StaffPersonUpdateDTO staffPersonUpdateDTO,
                                             HttpServletRequest request)
            throws CustomException {
        return accountService.staffUpdateProfile(staffPersonUpdateDTO, request);
    }

    @GetMapping("/customer/profile")
    public CustomerOutputDTO customerProfile(HttpServletRequest request)
            throws CustomException {
        return accountService.customerProfile(request);
    }

    @PostMapping("/customer/update-profile")
    public CustomerOutputDTO customerUpdateProfile(@Valid @RequestBody CustomerUpdateDTO customerUpdateDTO,
                                                   HttpServletRequest request)
            throws CustomException {
        return accountService.customerUpdateProfile(customerUpdateDTO, request);
    }

    @PostMapping("/change-password")
    public Message changePassword(@Valid @RequestBody ChangePassDTO changePassDTO,
                                  HttpServletRequest request)
            throws CustomException {
        return accountService.changePassword(changePassDTO.getOldPass(),
                changePassDTO.getNewPass(), request);
    }
}
