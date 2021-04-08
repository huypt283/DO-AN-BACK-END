package com.phamthehuy.doan.controller.common;

import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
import com.phamthehuy.doan.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
public class AccountController {
    @Autowired
    AccountService accountService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody SigninRequest signinRequest) throws Exception {
        return new ResponseEntity<>(accountService.signIn(signinRequest), HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    public MessageResponse customerSignup(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request) throws Exception {
        return accountService.customerSignup(signupRequest, request);
    }

    @GetMapping("/confirm")
    public MessageResponse confirmEmail(@RequestParam(value = "token") String token,
                                        @RequestParam String email) throws Exception {
        return accountService.confirmEmail(token, email);
    }

    @GetMapping("/refresh-token")
    public Map<String, String> refreshAccessToken(HttpServletRequest request) throws Exception {
        return accountService.refreshAccessToken(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@RequestParam String email) throws Exception {
        return accountService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) throws Exception {
        return accountService.resetPassword(resetPasswordRequest);
    }

    @GetMapping("/admin/profile")
    public StaffResponse staffProfile(@AuthenticationPrincipal UsernamePasswordAuthenticationToken currentUser)
            throws Exception {
        return accountService.staffProfile(currentUser);
    }

    @PostMapping("/admin/update-profile")
    public StaffResponse staffUpdateProfile(@Valid @RequestBody StaffPersonUpdateRequest staffPersonUpdateRequest,
                                            HttpServletRequest request)
            throws Exception {
        return accountService.staffUpdateProfile(staffPersonUpdateRequest, request);
    }

    @GetMapping("/customer/profile")
    public CustomerResponse customerProfile(HttpServletRequest request)
            throws Exception {
        return accountService.customerProfile(request);
    }

    @PostMapping("/customer/update-profile")
    public CustomerResponse customerUpdateProfile(@Valid @RequestBody CustomerUpdateRequest customerUpdateRequest,
                                                  HttpServletRequest request)
            throws Exception {
        return accountService.customerUpdateProfile(customerUpdateRequest, request);
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePassRequest changePassRequest,
                                          HttpServletRequest request)
            throws Exception {
        return accountService.changePassword(changePassRequest.getOldPass(),
                changePassRequest.getNewPass(), request);
    }
}
