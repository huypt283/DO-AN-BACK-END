package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.authentication.CustomUserDetailsService;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.exception.*;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.service.AccountService;
import com.phamthehuy.doan.util.MailSender;
import com.phamthehuy.doan.util.auth.JwtUtil;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    MailSender mailSender;

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    Helper helper;

    @Override
    public MessageResponse customerSignup(SignupRequest signupRequest, HttpServletRequest request) {
        //validate
        String numberMatcher = "[0-9]+";
        if (!signupRequest.getPhone().matches(numberMatcher))
            throw new BadRequestException("Số điện thoại phải là số");
        if (customerRepository.findByEmail(signupRequest.getEmail()) != null
                || staffRepository.findByEmail(signupRequest.getEmail()) != null)
            throw new DuplicatedException("Email này đã được sử dụng");

        try {
            //create token
            String token = helper.createToken(30);

            //create customer
            Customer customer = new Customer();
            BeanUtils.copyProperties(signupRequest, customer);
            customer.setAccountBalance(10000);
            customer.setPass(passwordEncoder.encode(signupRequest.getPassword()));
            customer.setToken(token);
            Customer newCustomer = customerRepository.save(customer);

            //send mail
            mailSender.send(
                    signupRequest.getEmail(),
                    "Xác nhận địa chỉ email",
                    "Click vào đường link sau để xác nhận email và kích hoạt tài khoản của bạn:<br/>" +
                            helper.getHostUrl(request.getRequestURL().toString(), "/sign-up") + "/confirm?token=" + token
                            + "&email=" + signupRequest.getEmail(),
                    "Thời hạn xác nhận, 10 phút kể từ khi đăng kí"
            );

            Thread deleteDisabledCustomer = new Thread() {
                @SneakyThrows
                @Override
                public void run() {
                    Thread.sleep(10 * 60 * 1000);
                    Optional<Customer> optionalCustomer =
                            customerRepository.findByCustomerIdAndEnabledFalse(newCustomer.getCustomerId());
                    optionalCustomer.ifPresent(customerRepository::delete);
                }
            };
            deleteDisabledCustomer.start();


            return new MessageResponse("Bạn hãy kiểm tra mail để xác nhận trong vòng 10 phút");
        } catch (Exception ex) {
            throw new InternalServerError("Có lỗi xảy ra khi đăng ký tài khoản. Vui lòng thử lại");
        }
    }

    @Override
    public MessageResponse confirmEmail(String token, String email) throws Exception {
        Staff staff = staffRepository.findByToken(token);
        if (staff != null) {
            if (!staff.getEmail().equals(email)) throw new BadRequestException("Email không chính xác");
            staff.setEnabled(true);
            staff.setToken(null);
            staffRepository.save(staff);
            return new MessageResponse("Xác nhận email thành công");
        } else {
            Customer customer = customerRepository.findByToken(token);

            if (customer != null) {
                if (!customer.getEmail().equals(email)) throw new BadRequestException("Email không chính xác");
                customer.setEnabled(true);
                customer.setToken(null);
                customerRepository.save(customer);
                return new MessageResponse("Xác nhận email thành công");
            } else
                throw new BadRequestException("Xác nhận email thất bại");
        }
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
            } else if (!customer.getEnabled())
                throw new AccessDeniedException("Cần xác nhận email để được kích hoạt");
            else if (customer.getDeleted())
                throw new AccessDeniedException("Khách hàng đang bị khóa");
        } else if (!staff.getEnabled())
            throw new AccessDeniedException("Cần xác nhận email để được kích hoạt");
        else if (staff.getDeleted())
            throw new AccessDeniedException("Nhân viên đang bị khóa");

        //login
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    signinRequest.getEmail(), signinRequest.getPassword()));
        } catch (DisabledException e) {
            throw new AccessDeniedException("Người dùng vô hiệu");
        } catch (BadCredentialsException e) {
            throw new UnauthenticatedException("Mật khẩu không đúng");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(signinRequest.getEmail());

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

    private String getRoleFromAuthority(Collection<? extends GrantedAuthority> authorities) {
        if (authorities.contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
            return "SUPER_ADMIN";
        else if (authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
            return "ADMIN";
        else
            return "CUSTOMER";
    }

    @Override
    public Map<String, String> refreshAccessToken(HttpServletRequest request) throws Exception {
        Map<String, String> returnMap = new HashMap<>();
        String refreshToken = extractRefreshTokenFromRequest(request);

        if (jwtTokenUtil.validateRefreshToken(refreshToken)) {
            String newAccessToken = "";
            Staff staff = staffRepository.findByRefreshToken(refreshToken);
            if (staff != null)
                newAccessToken = jwtTokenUtil.refreshAccessToken(staff.getEmail(), staff.getRole() ? "ROLE_SUPER_ADMIN" : "ROLE_ADMIN");
            else {
                Customer customer = customerRepository.findByRefreshToken(refreshToken);
                if (customer != null)
                    newAccessToken = jwtTokenUtil.refreshAccessToken(customer.getEmail(), "ROLE_CUSTOMER");
                else
                    throw new UnauthenticatedException("Token invalid");
            }

            returnMap.put("access_token", newAccessToken);
        }

        return returnMap;
    }

    @Override
    public MessageResponse forgotPassword(String email) throws Exception {
        Staff staff = null;
        staff = staffRepository.findByEmail(email);
        Customer customer = null;
        if (staff == null) customer = customerRepository.findByEmail(email);

        String token;

        if (staff != null) {
            if (!staff.getEnabled()) throw new BadRequestException("Email chưa được xác nhận");
            if (staff.getToken() != null)
                throw new BadRequestException("Email đổi mật khẩu đã được gửi, bạn hãy check lại mail");
            token = helper.createToken(31);
            staff.setToken(token);
            staffRepository.save(staff);
        } else if (customer != null) {
            if (!customer.getEnabled()) throw new BadRequestException("Email chưa được xác nhận");
            if (customer.getToken() != null)
                throw new BadRequestException("Email đổi mật khẩu đã được gửi, bạn hãy check lại mail");
            token = helper.createToken(31);
            customer.setToken(token);
            customerRepository.save(customer);
        } else {
            throw new BadRequestException("Email không tồn tại");
        }
        //send mail
        mailSender.send(
                email,
                "Quên mật khẩu",
                "Click vào đường link sau để tạo mới mật khẩu của bạn:<br/>" +
                        "dia chi frontend" + "/renew-password?token=" + token
                        + "&email=" + email,
                "Chúc bạn thành công"
        );
        return new MessageResponse("Thành công, bạn hãy check mail để tiếp tục");
    }

    @Override
    public MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) throws Exception {
        Staff staff = null;
        Customer customer = null;
        staff = staffRepository.findByToken(resetPasswordRequest.getToken());
        if (staff == null) customer = customerRepository.findByToken(resetPasswordRequest.getToken());
        if (staff != null) {
            if (!staff.getEmail().equals(resetPasswordRequest.getEmail()))
                throw new BadRequestException("Email không chính xác");
            staff.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
            staff.setToken(null);
            staffRepository.save(staff);
            return new MessageResponse("Làm mới mật khẩu thành công");
        } else if (customer != null) {
            if (!customer.getEmail().equals(resetPasswordRequest.getEmail()))
                throw new BadRequestException("Email không chính xác");
            customer.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
            customer.setToken(null);
            customerRepository.save(customer);
            return new MessageResponse("Làm mới mật khẩu thành cồng");
        } else throw new BadRequestException("Làm mới mật khẩu thất bại");
    }

    @Override
    public StaffResponse staffProfile(UserDetails currentUser) throws Exception {
        Staff staff = staffRepository.findByEmail(currentUser.getUsername());

        if (staff == null)
            throw new NotFoundException("Không tìm thấy người dùng");

        StaffResponse staffResponse = new StaffResponse();
        BeanUtils.copyProperties(staff, staffResponse);
        staffResponse.setBirthday(staff.getDob());
        staffResponse.setRole(staff.getRole() ? "SUPER_ADMIN" : "ADMIN");
        return staffResponse;
    }

    @Override
    public StaffResponse staffUpdateProfile(StaffPersonUpdateRequest staffPersonUpdateRequest,
                                            HttpServletRequest request) throws Exception {
        //validate
        String matchNumber = "[0-9]+";
        if (!staffPersonUpdateRequest.getCardId().matches(matchNumber))
            throw new BadRequestException("Số CMND phải là số");
        if (!staffPersonUpdateRequest.getPhone().matches(matchNumber))
            throw new BadRequestException("Số điện thoại phải là số");
        if (staffPersonUpdateRequest.getBirthday() >= System.currentTimeMillis())
            throw new BadRequestException("Ngày sinh phải trong quá khứ");

        //update
        try {
            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getEmailFromToken(jwt);
            Staff staff = staffRepository.findByEmail(email);
            if (staff == null)
                throw new BadRequestException("Token không hợp lệ");

            staff.setName(staffPersonUpdateRequest.getName());
            staff.setCardId(staffPersonUpdateRequest.getCardId());
            staff.setDob(new Date(staffPersonUpdateRequest.getBirthday()));
            staff.setGender(staffPersonUpdateRequest.isGender());
            staff.setAddress(staffPersonUpdateRequest.getAddress());
            staff.setPhone(staffPersonUpdateRequest.getPhone());
            staff.setImage(staffPersonUpdateRequest.getImage());

            staff = staffRepository.save(staff);

            StaffResponse staffResponse = new StaffResponse();
            BeanUtils.copyProperties(staff, staffResponse);
            staffResponse.setBirthday(staff.getDob());

            return staffResponse;
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException("Cập nhật thông tin cá nhân thất bại");
        }
    }

    @Override
    public CustomerResponse customerProfile(UserDetails currentUser) throws Exception {
        try {
            Customer customer = customerRepository.findByEmail(currentUser.getUsername());

            if (customer == null)
                throw new BadRequestException("Token không hợp lệ");

            CustomerResponse customerResponse = new CustomerResponse();
            BeanUtils.copyProperties(customer, customerResponse);

            customerResponse.setBirthday(customer.getDob());

            return customerResponse;
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException("Lỗi: người dùng không hợp lệ hoặc không tồn tại");
        }
    }

    @Override
    public CustomerResponse customerUpdateProfile(CustomerUpdateRequest customerUpdateRequest, HttpServletRequest request) throws Exception {
        //validate
        String matchNumber = "[0-9]+";
        if (customerUpdateRequest.getCardId() != null && !customerUpdateRequest.getCardId().equals("")) {
            if (!customerUpdateRequest.getCardId().matches(matchNumber))
                throw new BadRequestException("Số CMND phải là số");
            else if (customerUpdateRequest.getCardId().length() < 9 || customerUpdateRequest.getCardId().length() > 12)
                throw new BadRequestException("Số CMND phải gồm 9-12 số");
        }
        if (!customerUpdateRequest.getPhone().matches(matchNumber))
            throw new BadRequestException("Số điện thoại phải là số");
        if (customerUpdateRequest.getBirthday() >= System.currentTimeMillis())
            throw new BadRequestException("Ngày sinh phải trong quá khứ");

        //update
        try {
            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getEmailFromToken(jwt);
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Customer customer = customerRepository.findByEmail(email);
            if (customer == null) throw new BadRequestException("Token không hợp lệ");

            customer.setName(customerUpdateRequest.getName());
            customer.setGender(customerUpdateRequest.isGender());
            customer.setAddress(customerUpdateRequest.getAddress());
            customer.setPhone(customerUpdateRequest.getPhone());
            customer.setCardId(customerUpdateRequest.getCardId());
            customer.setDob(new Date(customerUpdateRequest.getBirthday()));
            customer.setImage(customerUpdateRequest.getImage());
            Customer newCustomer = customerRepository.save(customer);
            CustomerResponse customerResponse = modelMapper.map(newCustomer, CustomerResponse.class);
            customerResponse.setBirthday(newCustomer.getDob());
            return customerResponse;
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            //e.printStackTrace();
            throw new BadRequestException("Cập nhật thông tin cá nhân thất bại");
        }
    }

    @Override
    public MessageResponse changePassword(String oldPass, String newPass,
                                          HttpServletRequest request) throws Exception {
        try {
//            String token = extractJwtFromRequest(request);
//            String email = jwtTokenUtil.getEmailFromToken(token);
//            String role = jwtTokenUtil.getRoleFromToken(token);
//
//            if (email == null || email.trim().equals(""))
//                throw new BadRequestException("Token không hợp lệ");
//
//            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN")) {
//                Staff staff = staffRepository.findByEmail(email);
//                if (staff != null) {
//                    if (!passwordEncoder.matches(oldPass, staff.getPass()))
//                        throw new BadRequestException("Mật khẩu cũ không chính xác");
//                    staff.setPass(passwordEncoder.encode(newPass));
//                    staffRepository.save(staff);
//                    return new MessageResponse("Đổi mật khẩu cho nhân viên: " + staff.getEmail() + " thành công");
//                } else throw new BadRequestException("Không tìm thấy nhân viên hợp lệ");
//            } else {
//                Customer customer = customerRepository.findByEmail(email);
//                if (customer != null) {
//                    if (!passwordEncoder.matches(oldPass, customer.getPass()))
//                        throw new BadRequestException("Mật khẩu cũ không chính xác");
//                    customer.setPass(passwordEncoder.encode(newPass));
//                    customerRepository.save(customer);
//                    return new MessageResponse("Đổi mật khẩu khách hàng: " + customer.getEmail() + " thành công");
//                } else throw new BadRequestException("Không tìm thấy người dùng hợp lệ");
//            }
            return null;
        } catch (BadRequestException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException("Đổi mật khẩu thất bại");
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String extractRefreshTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("lynx");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("$xzyw.")) {
            return bearerToken.substring(6);
        }
        return null;
    }
}