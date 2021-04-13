package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.authentication.CustomUserDetailsService;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.exception.*;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.AccountResponse;
import com.phamthehuy.doan.model.response.CustomerResponse;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtTokenUtil;
    @Autowired
    private Helper helper;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public MessageResponse customerSignup(SignupRequest signupRequest, HttpServletRequest request) throws Exception {
        //validate
        if (!signupRequest.getPhone().matches("[0-9]+"))
            throw new BadRequestException("Số điện thoại phải là số");

        if (customerRepository.findByEmail(signupRequest.getEmail()) != null
                || staffRepository.findByEmail(signupRequest.getEmail()) != null)
            throw new ConflictException("Email này đã được sử dụng");

        try {
            //create token
            String token = helper.createToken(30);

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
                    "Xác nhận địa chỉ email",
                    "Click vào đường link sau để xác nhận email và kích hoạt tài khoản của bạn:<br/>" +
                            this.clientUrl + "/confirm?token=" + token
                            + "&email=" + signupRequest.getEmail(),
                    "Thời hạn xác nhận, 10 phút kể từ khi đăng kí"
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

            return new MessageResponse("Bạn hãy kiểm tra mail để xác nhận trong vòng 10 phút");
        } catch (Exception ex) {
            throw new InternalServerError("Có lỗi xảy ra khi đăng ký tài khoản. Vui lòng thử lại");
        }
    }

    @Override
    public MessageResponse confirmEmail(String token, String email) throws Exception {
        Staff staff = staffRepository.findByToken(token);
        if (staff != null) {
            if (!staff.getEmail().equals(email))
                throw new ConflictException("Email không chính xác");
            staff.setEnabled(true);
            staff.setToken(null);
            staffRepository.save(staff);
        } else {
            Customer customer = customerRepository.findByToken(token);
            if (customer != null) {
                if (!customer.getEmail().equals(email))
                    throw new ConflictException("Email không chính xác");
                customer.setEnabled(true);
                customer.setToken(null);
                customerRepository.save(customer);
            } else
                throw new ConflictException("Xác nhận email thất bại");
        }
        return new MessageResponse("Xác nhận email thành công");
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
                throw new NotFoundException("Email không tồn tại");
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
            } else if (customer != null && passwordEncoder.matches(signinRequest.getPassword(), customer.getPass())){
                roles = Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));

                userDetails = new User(customer.getEmail(), customer.getPass(), roles);
            } else
                throw new BadCredentialsException("Mật khẩu không đúng");

        } catch (BadCredentialsException e) {
            throw new UnauthenticatedException("Mật khẩu không đúng");
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
                throw new AccessDeniedException("Email đổi mật khẩu đã được gửi, bạn hãy check lại mail");

            token = helper.createToken(31);
            staff.setToken(token);
            staffRepository.save(staff);
        } else {
            Customer customer = customerRepository.findByEmail(email);
            if (customer != null) {
                validateCustomer(customer);
                if (customer.getToken() != null)
                    throw new AccessDeniedException("Email đổi mật khẩu đã được gửi, bạn hãy check lại mail");

                token = helper.createToken(31);
                customer.setToken(token);
                customerRepository.save(customer);
            } else {
                throw new BadRequestException("Email không tồn tại");
            }
        }
        //send mail
        mailSender.send(
                email,
                "Quên mật khẩu",
                "Click vào đường link sau để tạo mới mật khẩu của bạn:<br/>" +
                        this.clientUrl + "/reset-password?token=" + token
                        + "&email=" + email,
                "Chúc bạn thành công"
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

        return new MessageResponse("Thành công, bạn hãy kiểm tra mail để đặt lại mật khẩu trong 10 phút");
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) throws Exception {
        Staff staff = staffRepository.findByToken(resetPasswordRequest.getToken());

        if (staff != null) {
            if (!staff.getEmail().equals(resetPasswordRequest.getEmail()))
                throw new ConflictException("Email không chính xác");

            staff.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
            staff.setToken(null);
            staffRepository.save(staff);
        } else {
            Customer customer = customerRepository.findByToken(resetPasswordRequest.getToken());
            if (customer != null) {
                if (!customer.getEmail().equals(resetPasswordRequest.getEmail()))
                    throw new ConflictException("Email không chính xác");
                customer.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
                customer.setToken(null);
                customerRepository.save(customer);
            } else
                throw new NotFoundException("Không tìm thấy tài khoản cần đặt lại mật khẩu");
        }
        return new MessageResponse("Làm mới mật khẩu thành công");
    }

    @Override
    public AccountResponse getProfile(UserDetails currentUser) throws Exception {
        String email = currentUser.getUsername();
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        if (email == null || email.trim().equals(""))
            throw new UnauthenticatedException("Token không hợp lệ");

        if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
            Staff staff = staffRepository.findByEmail(currentUser.getUsername());

            if (staff == null)
                throw new NotFoundException("Không tìm thấy tài khoản");
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
                throw new BadRequestException("Không tìm thấy tài khoản");
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
        String email = currentUser.getUsername();
        String role = getRoleFromAuthority(currentUser.getAuthorities());

        if (email == null || email.trim().equals(""))
            throw new UnauthenticatedException("Token không hợp lệ");
        try {
            if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
                Staff staff = staffRepository.findByEmail(currentUser.getUsername());
                if (staff == null)
                    throw new NotFoundException("Không tìm thấy tài khoản");
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
                    throw new NotFoundException("Không tìm thấy tài khoản");
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
            throw new InternalServerError("Cập nhật thông tin cá nhân thất bại");
        }
    }

    @Override
    public CustomerResponse customerProfile(UserDetails currentUser) throws Exception {
        Customer customer = customerRepository.findByEmail(currentUser.getUsername());

        if (customer == null)
            throw new BadRequestException("Không tìm thấy tài khoản");
        else
            validateCustomer(customer);

        CustomerResponse customerResponse = new CustomerResponse();
        BeanUtils.copyProperties(customer, customerResponse);
        customerResponse.setBirthday(customer.getDob());
        customerResponse.setRole("CUSTOMER");
        return customerResponse;
    }

    @Override
    public CustomerResponse customerUpdateProfile(CustomerUpdateRequest customerUpdateRequest, UserDetails currentUser) throws Exception {
        //update
        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
        if (customer == null)
            throw new NotFoundException("Không tìm thấy tài khoản");
        else
            validateCustomer(customer);

        BeanUtils.copyProperties(customerUpdateRequest, customer);
        customer.setDob(customerUpdateRequest.getBirthday());

        customer = customerRepository.save(customer);

        CustomerResponse customerResponse = new CustomerResponse();
        BeanUtils.copyProperties(customer, customerResponse);
        customerResponse.setBirthday(customer.getDob());

        return customerResponse;
    }

    @Override
    public MessageResponse changePassword(ChangePassRequest changePassRequest, UserDetails currentUser) throws Exception {
        try {
            String email = currentUser.getUsername();
            String role = getRoleFromAuthority(currentUser.getAuthorities());

            if (email == null || email.trim().equals(""))
                throw new UnauthenticatedException("Token không hợp lệ");

            if (role.equals("SUPER_ADMIN") || role.equals("ADMIN")) {
                Staff staff = staffRepository.findByEmail(email);
                if (staff != null) {
                    validateStaff(staff);

                    if (!passwordEncoder.matches(changePassRequest.getOldPass(), staff.getPass()))
                        throw new BadRequestException("Mật khẩu cũ không chính xác");
                    staff.setPass(passwordEncoder.encode(changePassRequest.getNewPass()));
                    staffRepository.save(staff);
                    return new MessageResponse("Đổi mật khẩu cho nhân viên: " + staff.getEmail() + " thành công");
                } else
                    throw new NotFoundException("Không tìm thấy tài khoản");
            } else {
                Customer customer = customerRepository.findByEmail(email);
                if (customer != null) {
                    validateCustomer(customer);

                    if (!passwordEncoder.matches(changePassRequest.getOldPass(), customer.getPass()))
                        throw new BadRequestException("Mật khẩu cũ không chính xác");
                    customer.setPass(passwordEncoder.encode(changePassRequest.getNewPass()));
                    customerRepository.save(customer);
                    return new MessageResponse("Đổi mật khẩu khách hàng: " + customer.getEmail() + " thành công");
                } else
                    throw new NotFoundException("Không tìm thấy tài khoản");
            }
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException("Đổi mật khẩu thất bại");
        }
    }

    public void validateStaff(Staff staff) {
        if (BooleanUtils.isNotTrue(staff.getEnabled()))
            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
        else if (BooleanUtils.isTrue(staff.getDeleted()))
            throw new AccessDeniedException("Tài khoản này đã bị khoá");
    }

    public void validateCustomer(Customer customer) {
        if (BooleanUtils.isNotTrue(customer.getEnabled()))
            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
        else if (BooleanUtils.isTrue(customer.getDeleted()))
            throw new AccessDeniedException("Tài khoản này đã bị khoá");
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