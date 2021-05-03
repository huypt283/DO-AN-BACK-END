package com.phamthehuy.doan.authentication;

import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.exception.NotFoundException;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StaffRepository staffRepository;

    //load user by email return user
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        List<SimpleGrantedAuthority> roles = null;
        Staff staff = staffRepository.findByEmail(email);
        ;
        if (staff != null) {
            if (staff.getRole()) {
                roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            } else {
                roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            return new User(staff.getEmail(), staff.getPass(), roles);
        } else {
            Customer customer = customerRepository.findByEmail(email);

            if (customer != null) {
                roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                return new User(customer.getEmail(), customer.getPass(), roles);
            }
        }

        throw new NotFoundException("Không tìm thấy tài khoản");
    }
}