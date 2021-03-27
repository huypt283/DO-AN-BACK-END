package com.phamthehuy.doan.service;

import com.phamthehuy.doan.model.dto.input.LoginDTO;
import com.phamthehuy.doan.model.dto.input.ResetPasswordDTO;
import com.phamthehuy.doan.model.dto.input.SignupDTO;
import com.phamthehuy.doan.model.dto.output.Message;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AccountService {
    Message customerSignup(SignupDTO signupDTO, HttpServletRequest request);

    //sau nay chuyen sang void
    Message confirmEmail(String token, String email);

    Map<String, String> login(LoginDTO loginDTO) throws Exception;

    Map<String, String> refreshtoken(HttpServletRequest request) throws Exception;

    Message forgotPassword(String email);

    Message resetPassword(ResetPasswordDTO resetPasswordDTO);
}
