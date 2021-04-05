package com.phamthehuy.doan.authentication;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class JwtUtil {
    @Value("${auth.secret}")
    private String secret;

    @Value("${auth.expire}")
    private int jwtExpirationInMs;

    // generate token for user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
        if (roles.contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))) {
            claims.put("isSuperAdmin", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            claims.put("isAdmin", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            claims.put("isCustomer", true);
        }
        return doGenerateToken(claims, userDetails.getUsername());
    }

    public String doGenerateToken(Map<String, Object> claims, String subject) {

        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)).signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public boolean validateToken(String authToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        } catch (ExpiredJwtException ex) {
            throw ex;
        }
    }

    //return email from token
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    //return authority from token
    public List<SimpleGrantedAuthority> getRolesFromToken(String token) {
        Claims claims = getClaims(token);

        List<SimpleGrantedAuthority> roles = null;

        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Boolean isSuperAdmin = claims.get("isSuperAdmin", Boolean.class);
        Boolean isCustomer = claims.get("isCustomer", Boolean.class);

        if (isAdmin != null && isAdmin) {
            roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        if (isSuperAdmin != null && isSuperAdmin) {
            roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
        }

        if (isCustomer != null && isCustomer) {
            roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }
        return roles;
    }

    //return authority from token
    public String getRoleFromToken(String token) {
        Claims claims = getClaims(token);

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

    private Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
