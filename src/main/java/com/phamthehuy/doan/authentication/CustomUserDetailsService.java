package com.phamthehuy.doan.authentication;

import com.phamthehuy.doan.dao.CustomerRepository;
import com.phamthehuy.doan.dao.StaffRepository;
import com.phamthehuy.doan.model.entity.Customer;
import com.phamthehuy.doan.model.entity.Staff;
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

    final
    CustomerRepository customerRepository;

    final
    StaffRepository staffRepository;

    public CustomUserDetailsService(CustomerRepository customerRepository, StaffRepository staffRepository) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
    }

    //load user by email return user
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        List<SimpleGrantedAuthority> roles = null;
        Customer customer = null;
        Staff staff = null;
        customer = customerRepository.findByEmail(email);
        if (customer == null) staff = staffRepository.findByEmail(email);
        if (staff != null) {
            if (staff.getRole()) {
                roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
            } else {
                roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            return new User(staff.getEmail(), staff.getPass(), roles);
        } else if (customer != null) {
            roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
            return new User(customer.getEmail(), customer.getPass(), roles);
        }
        throw new UsernameNotFoundException("User not found with the name " + email);
    }
}