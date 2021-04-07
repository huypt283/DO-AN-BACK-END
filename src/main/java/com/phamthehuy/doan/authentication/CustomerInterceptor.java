package com.phamthehuy.doan.authentication;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.util.auth.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CustomerInterceptor implements HandlerInterceptor {
    JwtUtil jwtUtil = new JwtUtil();

    CustomJwtAuthenticationFilter customJwtAuthenticationFilter = new CustomJwtAuthenticationFilter(jwtUtil);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            String email = jwtUtil.getEmailFromToken(customJwtAuthenticationFilter.extractJwtFromRequest(request));

            if (email == null || email.trim().equals(""))
                throw new CustomException("Token không hợp lệ (filter)");
            request.setAttribute("email", email);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Token không hợp lệ (filter), hoặc đã hết hạn");
        }
    }
}
