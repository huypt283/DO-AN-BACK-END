package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AccountService {
    MessageResponse customerSignup(SignupRequest signupRequest, HttpServletRequest request) throws CustomException;

    //sau nay chuyen sang void
    MessageResponse confirmEmail(String token, String email) throws CustomException;

    Map<String, String> login(LoginRequest loginRequest) throws Exception;

    Map<String, String> refreshtoken(HttpServletRequest request) throws CustomException;

    MessageResponse forgotPassword(String email) throws CustomException;

    MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) throws CustomException;

    StaffResponse staffDetail(HttpServletRequest request) throws CustomException;

    StaffResponse staffUpdateProfile(StaffPersonUpdateRequest staffPersonUpdateRequest,
                                     HttpServletRequest request) throws CustomException;

    CustomerResponse customerProfile(HttpServletRequest request) throws CustomException;

    CustomerResponse customerUpdateProfile(CustomerUpdateRequest customerUpdateRequest,
                                           HttpServletRequest request) throws CustomException;

    MessageResponse changePassword(String oldPass, String newPass,
                                   HttpServletRequest request) throws CustomException;
}
