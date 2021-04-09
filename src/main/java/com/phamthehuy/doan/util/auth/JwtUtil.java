package com.phamthehuy.doan.util.auth;

import com.phamthehuy.doan.exception.UnauthenticatedException;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class JwtUtil {
    @Value("${auth.secret}")
    private String secret;

    @Value("${auth.expire}")
    private int jwtExpirationInMs;

    @Value("${auth.rfsecret}")
    private String rf_secret;

    @Value("${auth.rfexpire}")
    private int rf_jwtExpirationInMs;

    public String generateToken(UserDetails userDetails, boolean isRfToken) {
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
        if (roles.contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))) {
            claims.put("isSuperAdmin", true);
        } else if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            claims.put("isAdmin", true);
        } else if (roles.contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            claims.put("isCustomer", true);
        }
        return doGenerateToken(claims, userDetails.getUsername(), isRfToken);
    }

    public String refreshAccessToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        switch (role) {
            case "ROLE_SUPER_ADMIN":
                claims.put("isSuperAdmin", true);
                break;
            case "ROLE_ADMIN":
                claims.put("isAdmin", true);
                break;
            case "ROLE_CUSTOMER":
                claims.put("isCustomer", true);
                break;
        }
        return doGenerateToken(claims, email, false);
    }

    public String doGenerateToken(Map<String, Object> claims, String subject, boolean isRfToken) {
        return Jwts.builder().setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (isRfToken ? rf_jwtExpirationInMs : jwtExpirationInMs)))
                .signWith(SignatureAlgorithm.HS512, isRfToken ? rf_secret : secret)
                .compact();
    }

    public boolean validateAccess(Claims claims) {
        try {
            String role = getRoleFromClaims(claims);
            return !"".equals(role);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            throw new UnauthenticatedException("Token hết hạn sử dụng");
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parser().setSigningKey(rf_secret).parseClaimsJws(refreshToken);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            throw new UnauthenticatedException("Token hết hạn sử dụng");
        }
    }

    //return email from token
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getEmailFromClaims(Claims claims) {
        return claims.getSubject();
    }

    //return authority from token
    public List<SimpleGrantedAuthority> getRolesFromClaims(Claims claims) {
        List<SimpleGrantedAuthority> roles = null;

        Boolean isSuperAdmin = claims.get("isSuperAdmin", Boolean.class);
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Boolean isCustomer = claims.get("isCustomer", Boolean.class);

        if (isSuperAdmin != null && isSuperAdmin) {
            roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        }

        if (isAdmin != null && isAdmin) {
            roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        if (isCustomer != null && isCustomer) {
            roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }
        return roles;
    }

    public String getRoleFromClaims(Claims claims) {
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Boolean isSuperAdmin = claims.get("isSuperAdmin", Boolean.class);
        Boolean isCustomer = claims.get("isCustomer", Boolean.class);

        if (isSuperAdmin != null && isSuperAdmin) {
            return "ROLE_SUPER_ADMIN";
        }

        if (isAdmin != null && isAdmin) {
            return "ROLE_ADMIN";
        }

        if (isCustomer != null && isCustomer) {
            return "ROLE_CUSTOMER";
        }

        return "";
    }

    public Claims getClaims(String jwtToken) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(jwtToken).getBody();
    }
}
