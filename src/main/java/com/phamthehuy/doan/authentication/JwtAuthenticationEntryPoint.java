package com.phamthehuy.doan.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Exception exception = (Exception) request.getAttribute("exception");

        byte[] body;
        if (exception != null) {
            body = new ObjectMapper().writeValueAsBytes(Collections.singletonMap("message", exception.getMessage()));
        } else {
            body = new ObjectMapper().writeValueAsBytes(Collections.singletonMap("message", authException.getMessage()));
        }

        response.getOutputStream().write(body);
    }
}
