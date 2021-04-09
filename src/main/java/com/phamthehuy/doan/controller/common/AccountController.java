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
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<?> customerSignup(@Valid @RequestBody SignupRequest signupRequest, HttpServletRequest request) throws Exception {
        return new ResponseEntity<>(accountService.customerSignup(signupRequest, request), HttpStatus.OK);
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmEmail(@RequestParam(value = "token") String token,
                                          @RequestParam String email) throws Exception {
        return new ResponseEntity<>(accountService.confirmEmail(token, email), HttpStatus.OK);
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) throws Exception {
        return new ResponseEntity<>(accountService.refreshAccessToken(request), HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) throws Exception {
        return new ResponseEntity<>(accountService.forgotPassword(email), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) throws Exception {
        return new ResponseEntity<>(accountService.resetPassword(resetPasswordRequest), HttpStatus.OK);
    }

    @GetMapping("/admin/profile")
    public ResponseEntity<?> staffProfile(@AuthenticationPrincipal UserDetails currentUser)
            throws Exception {
        return new ResponseEntity<>(accountService.staffProfile(currentUser), HttpStatus.OK);
    }

    @PostMapping("/admin/update-profile")
    public ResponseEntity<?> staffUpdateProfile(@Valid @RequestBody StaffPersonUpdateRequest staffPersonUpdateRequest,
                                                @AuthenticationPrincipal UserDetails currentUser)
            throws Exception {
        return new ResponseEntity<>(accountService.staffUpdateProfile(staffPersonUpdateRequest, currentUser), HttpStatus.OK);
    }

    @GetMapping("/customer/profile")
    public ResponseEntity<?> customerProfile(@AuthenticationPrincipal UserDetails currentUser)
            throws Exception {
        return new ResponseEntity<>(accountService.customerProfile(currentUser), HttpStatus.OK);
    }

    @PostMapping("/customer/update-profile")
    public ResponseEntity<?> customerUpdateProfile(@Valid @RequestBody CustomerUpdateRequest customerUpdateRequest,
                                                   @AuthenticationPrincipal UserDetails currentUser)
            throws Exception {
        return new ResponseEntity<>(accountService.customerUpdateProfile(customerUpdateRequest, currentUser), HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePassRequest changePassRequest,
                                            @AuthenticationPrincipal UserDetails currentUser)
            throws Exception {
        return new ResponseEntity<>(accountService.changePassword(changePassRequest, currentUser), HttpStatus.OK);
    }
}
