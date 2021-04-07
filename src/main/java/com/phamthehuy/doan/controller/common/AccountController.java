package com.phamthehuy.doan.controller.common;

import com.phamthehuy.doan.authentication.CustomUserDetailsService;
import com.phamthehuy.doan.util.auth.JwtUtil;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
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
    public MessageResponse customerSignup(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request) throws CustomException {
        return accountService.customerSignup(signupRequest, request);
    }

    @GetMapping("/confirm")
    public MessageResponse confirmEmail(@RequestParam(value = "token-customer") String token,
                                        @RequestParam String email) throws CustomException {
        return accountService.confirmEmail(token, email);
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest loginRequest) throws Exception {
        //login danh cho khach hang ->role: customer
        //login danh cho nhan vien -> role: admin, super admin
        return accountService.login(loginRequest);
    }

    //Brear Token
    //isRefreshToken = true (Header)
    @GetMapping("/refreshtoken")
    public Map<String, String> refreshtoken(HttpServletRequest request) throws CustomException {
        return accountService.refreshtoken(request);
    }

    @GetMapping("/forgot")
    public MessageResponse forgotPassword(@RequestParam String email) throws CustomException {
        return accountService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) throws CustomException {
        return accountService.resetPassword(resetPasswordRequest);
    }

    @GetMapping("/admin/profile")
    public StaffResponse staffProfile(HttpServletRequest request)
            throws CustomException {
        return accountService.staffDetail(request);
    }

    @PostMapping("/admin/update-profile")
    public StaffResponse staffUpdateProfile(@Valid @RequestBody StaffPersonUpdateRequest staffPersonUpdateRequest,
                                            HttpServletRequest request)
            throws CustomException {
        return accountService.staffUpdateProfile(staffPersonUpdateRequest, request);
    }

    @GetMapping("/customer/profile")
    public CustomerResponse customerProfile(HttpServletRequest request)
            throws CustomException {
        return accountService.customerProfile(request);
    }

    @PostMapping("/customer/update-profile")
    public CustomerResponse customerUpdateProfile(@Valid @RequestBody CustomerUpdateRequest customerUpdateRequest,
                                                  HttpServletRequest request)
            throws CustomException {
        return accountService.customerUpdateProfile(customerUpdateRequest, request);
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePassRequest changePassRequest,
                                          HttpServletRequest request)
            throws CustomException {
        return accountService.changePassword(changePassRequest.getOldPass(),
                changePassRequest.getNewPass(), request);
    }
}
