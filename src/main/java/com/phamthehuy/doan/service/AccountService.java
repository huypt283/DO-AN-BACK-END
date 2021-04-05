package com.phamthehuy.doan.service;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.dto.input.*;
import com.phamthehuy.doan.model.dto.output.CustomerOutputDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.dto.output.StaffOutputDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AccountService {
    Message customerSignup(SignupDTO signupDTO, HttpServletRequest request) throws CustomException;

    //sau nay chuyen sang void
    Message confirmEmail(String token, String email) throws CustomException;

    Map<String, String> login(LoginDTO loginDTO) throws Exception;

    Map<String, String> refreshtoken(HttpServletRequest request) throws CustomException;

    Message forgotPassword(String email) throws CustomException;

    Message resetPassword(ResetPasswordDTO resetPasswordDTO) throws CustomException;

    StaffOutputDTO staffDetail(HttpServletRequest request) throws CustomException;

    StaffOutputDTO staffUpdateProfile(StaffPersonUpdateDTO staffPersonUpdateDTO,
                                      HttpServletRequest request) throws CustomException;

    CustomerOutputDTO customerProfile(HttpServletRequest request) throws CustomException;

    CustomerOutputDTO customerUpdateProfile(CustomerUpdateDTO customerUpdateDTO,
                                            HttpServletRequest request) throws CustomException;

    Message changePassword(String oldPass, String newPass,
                           HttpServletRequest request) throws CustomException;
}
