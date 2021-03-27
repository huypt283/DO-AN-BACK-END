package com.phamthehuy.doan.controller.account;

import com.phamthehuy.doan.authentication.CustomUserDetailsService;
import com.phamthehuy.doan.authentication.JwtUtil;
import com.phamthehuy.doan.model.dto.input.LoginDTO;
import com.phamthehuy.doan.model.dto.input.ResetPasswordDTO;
import com.phamthehuy.doan.model.dto.input.SignupDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.service.AccountService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class AccountController {
    final
    AccountService accountService;

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsService userDetailsService;

    private final JwtUtil jwtTokenUtil;

    public AccountController(AccountService accountService, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtTokenUtil) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    @PostMapping("/sign-up")
    public Message customerSignup(@RequestBody SignupDTO signupDTO, HttpServletRequest request) {
        return accountService.customerSignup(signupDTO, request);
    }

    @GetMapping("/confirm")
    public Message confirmEmail(@RequestParam(value = "token-customer") String token,
                                @RequestParam String email) {
        return accountService.confirmEmail(token, email);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginDTO loginDTO) throws Exception {
        //login danh cho khach hang ->role: customer
        //login danh cho nhan vien -> role: admin, super admin
        return accountService.login(loginDTO);
    }

    //Brear Token
    //isRefreshToken = true (Header)
    @GetMapping("/refreshtoken")
    public Map<String, String> refreshtoken(HttpServletRequest request) throws Exception {
        return accountService.refreshtoken(request);
    }

    @GetMapping("/forgot")
    public Message forgotPassword(@RequestParam String email){
        return accountService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public Message resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO){
        return accountService.resetPassword(resetPasswordDTO);
    }
}
