package com.phamthehuy.doan.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {
    private Long id;
    private String email;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String roleToString() {
        StringBuilder s = new StringBuilder(",");
        for (GrantedAuthority grantedAuthority : getAuthorities())
            s.append(" ").append(grantedAuthority.getAuthority());
        return s.toString();
    }
}