//package com.phamthehuy.doan.authentication;
//
//import com.phamthehuy.doan.exception.BadRequestException;
//import com.phamthehuy.doan.util.auth.JwtUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//public class CustomerInterceptor implements HandlerInterceptor {
//    @Autowired
//    JwtUtil jwtUtil;
//
//    @Autowired
//    CustomJwtAuthenticationFilter customJwtAuthenticationFilter;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
//            throws Exception {
//        try {
//            String email = jwtUtil.getEmailFromToken(customJwtAuthenticationFilter.extractJwtFromRequest(request));
//
//            if (email == null || email.trim().equals(""))
//                throw new BadRequestException("Token không hợp lệ (filter)");
//            request.setAttribute("email", email);
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new BadRequestException("Token không hợp lệ (filter), hoặc đã hết hạn");
//        }
//    }
//}
