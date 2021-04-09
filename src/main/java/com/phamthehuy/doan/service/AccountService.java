package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AccountService {
    MessageResponse customerSignup(SignupRequest signupRequest, HttpServletRequest request) throws Exception;

    MessageResponse confirmEmail(String token, String email) throws Exception;

    Map<String, String> signIn(SigninRequest signinRequest) throws Exception;

    Map<String, String> refreshAccessToken(HttpServletRequest request) throws Exception;

    MessageResponse forgotPassword(String email) throws Exception;

    MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) throws Exception;

    StaffResponse staffProfile(UserDetails currentUser) throws Exception;

    StaffResponse staffUpdateProfile(StaffPersonUpdateRequest staffPersonUpdateRequest,
                                     UserDetails currentUser) throws Exception;

    CustomerResponse customerProfile(UserDetails currentUser) throws Exception;

    CustomerResponse customerUpdateProfile(CustomerUpdateRequest customerUpdateRequest,
                                           UserDetails currentUser) throws Exception;

    MessageResponse changePassword(ChangePassRequest changePassRequest,
                                   UserDetails currentUser) throws Exception;
}
