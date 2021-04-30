package com.phamthehuy.doan.authentication;

import com.phamthehuy.doan.util.auth.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomJwtAuthenticationFilter extends OncePerRequestFilter {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String jwtToken = extractJwtFromRequest(request);
            if (StringUtils.hasText(jwtToken)) {
                Claims claims = jwtTokenUtil.getClaims(jwtToken);
                if (jwtTokenUtil.validateAccess(claims)) {
                    UserDetails userDetails = new User(jwtTokenUtil.getEmailFromClaims(claims),
                            "", jwtTokenUtil.getRolesFromClaims(claims));

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } else
                    throw new BadCredentialsException("INVALID_CREDENTIALS");
            } else
                throw new BadCredentialsException("INVALID_CREDENTIALS");
        } catch (ExpiredJwtException | BadCredentialsException ex) {
            request.setAttribute("exception", ex);
        }
        chain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}