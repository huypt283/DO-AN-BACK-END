package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.exception.*;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.AccountResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.AccountService;
import com.phamthehuy.doan.util.MailSender;
import com.phamthehuy.doan.util.auth.JwtUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private Helper helper;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public MessageResponse customerSignUp(SignupRequest signupRequest) throws Exception {
        //validate
        if (customerRepository.findByEmail(signupRequest.getEmail()) != null
                || staffRepository.findByEmail(signupRequest.getEmail()) != null)
            throw new ConflictException("Email n??y ???? ???????c s??? d???ng");

        try {
            //create token
            String token = helper.createUserToken(30);

            //create customer
            Customer customer = new Customer();
            BeanUtils.copyProperties(signupRequest, customer);
            customer.setAccountBalance(10000);
            customer.setPass(passwordEncoder.encode(signupRequest.getPassword()));
            customer.setToken(token);
            customer.setEnabled(false);
            customerRepository.save(customer);

            //send mail
            mailSender.send(
                    signupRequest.getEmail(),
                    "X??c nh???n ?????a ch??? email",
                    "Click v??o ???????ng link sau ????? x??c nh???n email v?? k??ch ho???t t??i kho???n c???a b???n:<br/>" +
                            this.clientUrl + "/xac-nhan-email?token=" + token
                            + "&email=" + signupRequest.getEmail(),
                    "Th???i h???n x??c nh???n, 10 ph??t k??? t??? khi ????ng k??"
            );

            Thread deleteUnconfirmed = new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    Thread.sleep(10 * 60 * 1000);
                    Customer customer = customerRepository.findByToken(token);
                    if (customer != null) {
                        customerRepository.delete(customer);
                    }
                }
            };
            deleteUnconfirmed.start();

            return new MessageResponse("B???n h??y ki???m tra mail ????? x??c nh???n trong v??ng 10 ph??t");
        } catch (Exception ex) {
            throw new InternalServerError("C?? l???i x???y ra khi ????ng k?? t??i kho???n. Vui l??ng th??? l???i");
        }
    }

    @Override
    public MessageResponse confirmEmail(String token, String email) throws Exception {
        Staff staff = staffRepository.findByToken(token);
        if (staff != null) {
            if (!staff.getEmail().equals(email))
                throw new ConflictException("Email kh??ng ch??nh x??c");
            staff.setEnabled(true);
            staff.setToken(null);
            staffRepository.save(staff);
        } else {
            Customer customer = customerRepository.findByToken(token);
            if (customer != null) {
                if (!customer.getEmail().equals(email))
                    throw new ConflictException("Email kh??ng ch??nh x??c");
                customer.setEnabled(true);
                customer.setToken(null);
                customerRepository.save(customer);
            } else
                throw new ConflictException("Link x??c nh???n kh??ng h???p l??? ho???c ???? h???t h???n");
        }
        return new MessageResponse("X??c nh???n email th??nh c??ng. B???n c?? th??? ????ng nh???p ngay b??y gi???");
    }

    @Override
    public Map<String, String> signIn(SigninRequest signinRequest) throws Exception {
        Map<String, String> returnMap = new HashMap<>();
        Customer customer = null;
        Staff staff = staffRepository.findByEmail(signinRequest.getEmail());
        //validate
        if (staff == null) {
            customer = customerRepository.findByEmail(signinRequest.getEmail());
            if (customer == null) {
                throw new NotFoundException("T??i kho???n kh??ng t???n t???i");
            } else
                validateCustomer(customer);
        } else
            validateStaff(staff);

        UserDetails userDetails = null;
        List<SimpleGrantedAuthority> roles = null;
        //login
        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                    signinRequest.getEmail(), signinRequest.getPassword()));
            if (staff != null && passwordEncoder.matches(signinRequest.getPassword(), staff.getPass())) {
                if (BooleanUtils.isTrue(staff.getRole())) {
                    roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                } else {
                    roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                userDetails = new User(staff.getEmail(), staff.getPass(), roles);
            } else if (customer != null && passwordEncoder.matches(signinRequest.getPassword(), customer.getPass())) {
                roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

                userDetails = new User(customer.getEmail(), customer.getPass(), roles);
            } else
                throw new UnauthenticatedException("M???t kh???u kh??ng ????ng");

        } catch (BadCredentialsException e) {
            throw new UnauthenticatedException("M???t kh???u kh??ng ????ng");
        }

//        final UserDetails userDetails = userDetailsService.loadUserByUsername(signinRequest.getEmail());

        final String token = jwtTokenUtil.generateToken(userDetails, false);
        final String refreshToken = jwtTokenUtil.generateToken(userDetails, true);

        if (staff != null) {
            staff.setRefreshToken(refreshToken);
            staffRepository.save(staff);
        } else {
            customer.setRefreshToken(refreshToken);
            customerRepository.save(customer);
        }

        String role = getRoleFromAuthority(userDetails.getAuthorities());

        if (staff != null && (role.equalsIgnoreCase("SUPER_ADMIN") ||
                role.equalsIgnoreCase("ADMIN"))) {
            returnMap.put("id", staff.getStaffId().toString());
            returnMap.put("name", staff.getName());
            returnMap.put("email", staff.getEmail());
            returnMap.put("image", staff.getImage());
        } else if (customer != null) {
            returnMap.put("id", customer.getCustomerId().toString());
            returnMap.put("name", customer.getName());
            returnMap.put("email", customer.getEmail());
            returnMap.put("image", customer.getImage());
        }

        returnMap.put("role", role);
        returnMap.put("access_token", token);
        returnMap.put("refresh_token", refreshToken);

        return returnMap;
    }

    @Override
    public Map<String, String> refreshAccessToken(HttpServletRequest request) throws Exception {
        Map<String, String> returnMap = new HashMap<>();
        String refreshToken = extractRefreshTokenFromRequest(request);

        if (jwtTokenUtil.validateRefreshToken(refreshToken)) {
            String newAccessToken = "";
            Staff staff = staffRepository.findByRefreshToken(refreshToken);
            if (staff != null) {
                validateStaff(staff);
                newAccessToken = jwtTokenUtil.refreshAccessToken(staff.getEmail(), staff.getRole() ? "ROLE_SUPER_ADMIN" : "ROLE_ADMIN");
            } else {
                Customer customer = customerRepository.findByRefreshToken(refreshToken);
                if (customer != null) {
                    validateCustomer(customer);
                    newAccessToken = jwtTokenUtil.refreshAccessToken(customer.getEmail(), "ROLE_CUSTOMER");
                } else
                    throw new UnauthenticatedException("Token invalid");
            }

            returnMap.put("access_token", newAccessToken);
        }

        return returnMap;
    }

    @Override
    public MessageResponse forgotPassword(String email) throws Exception {
        Staff staff = staffRepository.findByEmail(email);
        String token;

        if (staff != null) {
            validateStaff(staff);
            if (staff.getToken() != null)
                throw new ConflictException("Email ?????i m???t kh???u ???? ???????c g???i, b???n h??y ki???m tra l???i email");

            token = helper.createUserToken(31);
            staff.setToken(token);
            staffRepository.save(staff);
        } else {
            Customer customer = customerRepository.findByEmail(email);
            if (customer != null) {
                validateCustomer(customer);
                if (customer.getToken() != null)
                    throw new ConflictException("Email ?????i m???t kh???u ???? ???????c g???i, b???n h??y ki???m tra l???i email");

                token = helper.createUserToken(31);
                customer.setToken(token);
                customerRepository.save(customer);
            } else {
                throw new NotFoundException("Email kh??ng t???n t???i");
            }
        }
        //send mail
        mailSender.send(
                email,
                "Qu??n m???t kh???u",
                "Click v??o ???????ng link sau ????? t???o m???i m???t kh???u c???a b???n:<br/>" +
                        this.clientUrl + "/dat-lai-mat-khau?token=" + token
                        + "&email=" + email,
                "Ch??c b???n th??nh c??ng"
        );

        Thread deleteToken = new Thread() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.sleep(10 * 60 * 1000);
                Customer customer = customerRepository.findByToken(token);
                if (customer != null) {
                    customer.setToken(null);
                    customerRepository.save(customer);
                }
            }
        };
        deleteToken.start();

        return new MessageResponse("B???n h??y ki???m tra mail ????? ?????t l???i m???t kh???u trong 10 ph??t");
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) throws Exception {
        Staff staff = staffRepository.findByToken(resetPasswordRequest.getToken());

        if (staff != null) {
            if (!staff.getEmail().equals(resetPasswordRequest.getEmail()))
                throw new ConflictException("Email kh??ng ch??nh x??c");

            staff.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
            staff.setToken(null);
            staffRepository.save(staff);
        } else {
            Customer customer = customerRepository.findByToken(resetPasswordRequest.getToken());
            if (customer != null) {
                if (!customer.getEmail().equals(resetPasswordRequest.getEmail()))
                    throw new ConflictException("Email kh??ng ch??nh x??c");
                customer.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
                customer.setToken(null);
                customerRepository.save(customer);
            } else
                throw new NotFoundException("???????ng d???n ?????t l???i kh??ng h???p l??? ho???c ???? h???t h???n");
        }
        return new MessageResponse("?????t l???i m???t kh???u th??nh c??ng");
    }

    @Override
    public AccountResponse getProfile(UserDetails currentUser) throws Exception {
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
            Staff staff = staffRepository.findByEmail(currentUser.getUsername());

            if (staff == null)
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
            else
                validateStaff(staff);

            AccountResponse staffResponse = new AccountResponse();
            BeanUtils.copyProperties(staff, staffResponse);
            staffResponse.setAccountId(staff.getStaffId());
            staffResponse.setBirthday(staff.getDob());
            staffResponse.setRole(staff.getRole() ? "SUPER_ADMIN" : "ADMIN");

            return staffResponse;
        } else {
            Customer customer = customerRepository.findByEmail(currentUser.getUsername());

            if (customer == null)
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
            else
                validateCustomer(customer);

            AccountResponse customerResponse = new AccountResponse();
            BeanUtils.copyProperties(customer, customerResponse);
            customerResponse.setAccountId(customer.getCustomerId());
            customerResponse.setBirthday(customer.getDob());
            customerResponse.setRole("CUSTOMER");

            return customerResponse;
        }
    }

    @Override
    public AccountResponse updateProfile(AccountUpdateRequest accountUpdateRequest,
                                         UserDetails currentUser) throws Exception {
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        try {
            if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
                Staff staff = staffRepository.findByEmail(currentUser.getUsername());
                if (staff == null)
                    throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
                else
                    validateStaff(staff);

                BeanUtils.copyProperties(accountUpdateRequest, staff);
                staff.setDob(accountUpdateRequest.getBirthday());

                staff = staffRepository.save(staff);

                AccountResponse staffResponse = new AccountResponse();
                BeanUtils.copyProperties(staff, staffResponse);
                staffResponse.setAccountId(staff.getStaffId());
                staffResponse.setBirthday(staff.getDob());

                return staffResponse;
            } else {
                Customer customer = customerRepository.findByEmail(currentUser.getUsername());
                if (customer == null)
                    throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
                else
                    validateCustomer(customer);

                BeanUtils.copyProperties(accountUpdateRequest, customer);
                customer.setDob(accountUpdateRequest.getBirthday());

                customer = customerRepository.save(customer);

                AccountResponse customerResponse = new AccountResponse();
                BeanUtils.copyProperties(customer, customerResponse);
                customerResponse.setAccountId(customer.getCustomerId());
                customerResponse.setBirthday(customer.getDob());

                return customerResponse;
            }
        } catch (Exception e) {
            throw new InternalServerError("C???p nh???t th??ng tin c?? nh??n th???t b???i");
        }
    }

    @Override
    public MessageResponse changePassword(ChangePassRequest changePassRequest, UserDetails currentUser) throws Exception {
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
            Staff staff = staffRepository.findByEmail(currentUser.getUsername());
            if (staff != null) {
                validateStaff(staff);

                if (!passwordEncoder.matches(changePassRequest.getOldPass(), staff.getPass()))
                    throw new ConflictException("M???t kh???u c?? kh??ng ch??nh x??c");
                staff.setPass(passwordEncoder.encode(changePassRequest.getNewPass()));
                staffRepository.save(staff);
                return new MessageResponse("?????i m???t kh???u cho nh??n vi??n: " + staff.getEmail() + " th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        } else {
            Customer customer = customerRepository.findByEmail(currentUser.getUsername());
            if (customer != null) {
                validateCustomer(customer);

                if (!passwordEncoder.matches(changePassRequest.getOldPass(), customer.getPass()))
                    throw new ConflictException("M???t kh???u c?? kh??ng ch??nh x??c");
                customer.setPass(passwordEncoder.encode(changePassRequest.getNewPass()));
                customerRepository.save(customer);
                return new MessageResponse("?????i m???t kh???u kh??ch h??ng: " + customer.getEmail() + " th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        }
    }

    @Override
    public MessageResponse changeAvatar(ChangeAvatarRequest changeAvatarRequest, UserDetails currentUser) throws Exception {
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
            Staff staff = staffRepository.findByEmail(currentUser.getUsername());
            if (staff != null) {
                validateStaff(staff);

                staff.setImage(changeAvatarRequest.getImage());
                staffRepository.save(staff);
                return new MessageResponse("C???p nh???t ???nh ?????i di???n th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        } else {
            Customer customer = customerRepository.findByEmail(currentUser.getUsername());
            if (customer != null) {
                validateCustomer(customer);

                customer.setImage(changeAvatarRequest.getImage());
                customerRepository.save(customer);
                return new MessageResponse("C???p nh???t ???nh ?????i di???n th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        }
    }

    @Override
    public MessageResponse deleteAvatar(UserDetails currentUser) throws Exception {
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
            Staff staff = staffRepository.findByEmail(currentUser.getUsername());
            if (staff != null) {
                validateStaff(staff);

                staff.setImage(null);
                staffRepository.save(staff);
                return new MessageResponse("X??a ???nh ?????i di???n th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        } else {
            Customer customer = customerRepository.findByEmail(currentUser.getUsername());
            if (customer != null) {
                validateCustomer(customer);

                customer.setImage(null);
                customerRepository.save(customer);
                return new MessageResponse("X??a ???nh ?????i di???n th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        }
    }

    @Override
    public MessageResponse signOut(UserDetails currentUser) throws Exception {
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
            Staff staff = staffRepository.findByEmail(currentUser.getUsername());
            if (staff != null) {
                validateStaff(staff);

                staff.setRefreshToken(null);
                staffRepository.save(staff);
                return new MessageResponse("????ng xu???t th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        } else {
            Customer customer = customerRepository.findByEmail(currentUser.getUsername());
            if (customer != null) {
                validateCustomer(customer);

                customer.setRefreshToken(null);
                customerRepository.save(customer);
                return new MessageResponse("????ng xu???t th??nh c??ng");
            } else
                throw new NotFoundException("Kh??ng t??m th???y t??i kho???n");
        }
    }

    public void validateStaff(Staff staff) {
        if (BooleanUtils.isNotTrue(staff.getEnabled()))
            throw new AccessDeniedException("T??i kho???n n??y ch??a ???????c k??ch ho???t");
        else if (BooleanUtils.isTrue(staff.getDeleted()))
            throw new AccessDeniedException("T??i kho???n n??y ???? b??? kho??");
    }

    public void validateCustomer(Customer customer) {
        if (BooleanUtils.isNotTrue(customer.getEnabled()))
            throw new AccessDeniedException("T??i kho???n n??y ch??a ???????c k??ch ho???t");
        else if (BooleanUtils.isTrue(customer.getDeleted()))
            throw new AccessDeniedException("T??i kho???n n??y ???? b??? kho??");
    }

    private String getRoleFromAuthority(Collection<? extends GrantedAuthority> authorities) {
        if (authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
            return "SUPER_ADMIN";
        else if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
            return "ADMIN";
        else
            return "CUSTOMER";
    }

    private String extractRefreshTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("lynx");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("$xzyw.")) {
            return bearerToken.substring(6);
        }
        return null;
    }
}