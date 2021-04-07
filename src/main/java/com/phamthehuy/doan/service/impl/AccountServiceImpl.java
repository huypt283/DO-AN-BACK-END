package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.authentication.CustomUserDetailsService;
import com.phamthehuy.doan.util.auth.JwtUtil;
import com.phamthehuy.doan.model.request.*;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.response.CustomerResponse;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.StaffResponse;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.Staff;
import com.phamthehuy.doan.service.AccountService;
import com.phamthehuy.doan.util.MailSender;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {
    final
    PasswordEncoder passwordEncoder;

    final
    CustomerRepository customerRepository;

    final
    MailSender mailSender;

    final
    StaffRepository staffRepository;

    final
    AuthenticationManager authenticationManager;

    final
    CustomUserDetailsService userDetailsService;

    final
    JwtUtil jwtTokenUtil;

    final
    Helper helper;

    public AccountServiceImpl(PasswordEncoder passwordEncoder, CustomerRepository customerRepository, MailSender mailSender, StaffRepository staffRepository, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtTokenUtil, Helper helper) {
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.mailSender = mailSender;
        this.staffRepository = staffRepository;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.helper = helper;
    }

    @Override
    public MessageResponse customerSignup(SignupRequest signupRequest, HttpServletRequest request) throws CustomException {
        // same as BeanUtils
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        //validate
        String numberMatcher = "[0-9]+";
        if (!signupRequest.getPhone().matches(numberMatcher))
            throw new CustomException("Số điện thoại phải là số");
        if (customerRepository.findByEmail(signupRequest.getEmail()) != null
                || staffRepository.findByEmail(signupRequest.getEmail()) != null)
            throw new CustomException("Email đã được sử dụng");

        try {
            //create token
            String token = helper.createToken(30);

            //create customer
            Customer customer = modelMapper.map(signupRequest, Customer.class);
            customer.setAccountBalance(10000);
            customer.setPass(passwordEncoder.encode(signupRequest.getPass()));
            customer.setToken(token);
            Customer newCustomer = customerRepository.save(customer);

            //send mail
            mailSender.send(
                    signupRequest.getEmail(),
                    "Xác nhận địa chỉ email",
                    "Click vào đường link sau để xác nhận email và kích hoạt tài khoản của bạn:<br/>" +
                            helper.getHostUrl(request.getRequestURL().toString(), "/sign-up") + "/confirm?token-customer=" + token
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

            return new MessageResponse("Bạn hãy check mail để xác nhận, trong vòng 10 phút");
        } catch (Exception e) {
            throw new CustomException("Đăng kí thất bại");
        }
    }

    //về sau chuyển thành void, return redirect
    @Override
    public MessageResponse confirmEmail(String token, String email) throws CustomException {
        Staff staff = null;
        Customer customer = customerRepository.findByToken(token);
        if (customer == null) staff = staffRepository.findByToken(token);
        if (staff != null) {
            if (!staff.getEmail().equals(email)) throw new CustomException("Email không chính xác");
            staff.setEnabled(true);
            staff.setToken(null);
            staffRepository.save(staff);

            //nen lam redirect
            return new MessageResponse("Xác nhận email thành công");
        } else if (customer != null) {
            if (!customer.getEmail().equals(email)) throw new CustomException("Email không chính xác");
            customer.setEnabled(true);
            customer.setToken(null);
            customerRepository.save(customer);

            //nen lam redirect
            return new MessageResponse("Xác nhận email thành công");
        } else throw new CustomException("Xác nhận email thất bại");
    }

    @Override
    public Map<String, String> login(LoginRequest loginRequest) throws Exception {
        Map<String, String> returnMap = new HashMap<>();
        Customer customer = customerRepository.findByEmail(loginRequest.getEmail());
        Staff staff = staffRepository.findByEmail(loginRequest.getEmail());
        //validate
        if (customer == null) {
            if (staff == null) {
                throw new CustomException("Email không tồn tại");
            } else if (!staff.getEnabled())
                throw new CustomException("Email chưa kích hoạt");
            else if (staff.getDeleted())
                throw new CustomException("Nhân viên đang bị khóa");
        } else if (!customer.getEnabled())
            throw new CustomException("Email chưa được kích hoạt");
        else if (customer.getDeleted())
            throw new CustomException("Khách hàng đang bị khóa");

        //login
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(), loginRequest.getPass()));
        } catch (DisabledException e) {
            throw new Exception("Người dùng vô hiệu", e);
        } catch (BadCredentialsException e) {
            //throw new Exception("Bad credentials", e);
            throw new CustomException("Mật khẩu không đúng");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        final String token = jwtTokenUtil.generateToken(userDetails);

        String role;
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
            role = "SUPER_ADMIN";
        else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
            role = "ADMIN";
        else role = "CUSTOMER";

        if (role.equalsIgnoreCase("SUPER_ADMIN") ||
                role.equalsIgnoreCase("ADMIN")) {
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
        returnMap.put("token", token);

        return returnMap;
    }

    @Override
    public Map<String, String> refreshtoken(HttpServletRequest request) throws CustomException {
        try {
            // From the HttpRequest get the claims
            DefaultClaims claims = (io.jsonwebtoken.impl.DefaultClaims) request.getAttribute("claims");

            Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
            String token = jwtTokenUtil.doGenerateToken(expectedMap, expectedMap.get("sub").toString());
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("token", token);
            return returnMap;
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Refresh token thất bại");
        }
    }

    @Override
    public MessageResponse forgotPassword(String email) throws CustomException {
        Staff staff = null;
        staff = staffRepository.findByEmail(email);
        Customer customer = null;
        if (staff == null) customer = customerRepository.findByEmail(email);

        String token;

        if (staff != null) {
            if (!staff.getEnabled()) throw new CustomException("Email chưa được xác nhận");
            if (staff.getToken() != null)
                throw new CustomException("Email đổi mật khẩu đã được gửi, bạn hãy check lại mail");
            token = helper.createToken(31);
            staff.setToken(token);
            staffRepository.save(staff);
        } else if (customer != null) {
            if (!customer.getEnabled()) throw new CustomException("Email chưa được xác nhận");
            if (customer.getToken() != null)
                throw new CustomException("Email đổi mật khẩu đã được gửi, bạn hãy check lại mail");
            token = helper.createToken(31);
            customer.setToken(token);
            customerRepository.save(customer);
        } else {
            throw new CustomException("Email không tồn tại");
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
    public MessageResponse resetPassword(ResetPasswordRequest resetPasswordRequest) throws CustomException {
        Staff staff = null;
        Customer customer = null;
        staff = staffRepository.findByToken(resetPasswordRequest.getToken());
        if (staff == null) customer = customerRepository.findByToken(resetPasswordRequest.getToken());
        if (staff != null) {
            if (!staff.getEmail().equals(resetPasswordRequest.getEmail()))
                throw new CustomException("Email không chính xác");
            staff.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
            staff.setToken(null);
            staffRepository.save(staff);
            return new MessageResponse("Làm mới mật khẩu thành công");
        } else if (customer != null) {
            if (!customer.getEmail().equals(resetPasswordRequest.getEmail()))
                throw new CustomException("Email không chính xác");
            customer.setPass(passwordEncoder.encode(resetPasswordRequest.getPassword()));
            customer.setToken(null);
            customerRepository.save(customer);
            return new MessageResponse("Làm mới mật khẩu thành cồng");
        } else throw new CustomException("Làm mới mật khẩu thất bại");
    }

    @Override
    public StaffResponse staffDetail(HttpServletRequest request) throws CustomException {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getEmailFromToken(jwt);
            Staff newStaff = staffRepository.findByEmail(email);

            if (newStaff == null)
                throw new CustomException("Token không hợp lệ");

            StaffResponse staffResponse = modelMapper.map(newStaff, StaffResponse.class);
            staffResponse.setBirthday(newStaff.getDob().getTime());
            return staffResponse;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            throw new CustomException("Lỗi: người dùng không hợp lệ hoặc không tồn tại");
        }
    }

    @Override
    public StaffResponse staffUpdateProfile(StaffPersonUpdateRequest staffPersonUpdateRequest,
                                            HttpServletRequest request) throws CustomException {
        //validate
        String matchNumber = "[0-9]+";
        if (!staffPersonUpdateRequest.getCardId().matches(matchNumber))
            throw new CustomException("Số CMND phải là số");
        if (!staffPersonUpdateRequest.getPhone().matches(matchNumber))
            throw new CustomException("Số điện thoại phải là số");
        if (staffPersonUpdateRequest.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ngày sinh phải trong quá khứ");

        //update
        try {
            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getEmailFromToken(jwt);
            Staff staff = staffRepository.findByEmail(email);
            if (staff == null) throw new CustomException("Token không hợp lệ");

            staff.setName(staffPersonUpdateRequest.getName());
            staff.setCardId(staffPersonUpdateRequest.getCardId());
            staff.setDob(new Date(staffPersonUpdateRequest.getBirthday()));
            staff.setGender(staffPersonUpdateRequest.isGender());
            staff.setAddress(staffPersonUpdateRequest.getAddress());
            staff.setPhone(staffPersonUpdateRequest.getPhone());
            staff.setImage(staffPersonUpdateRequest.getImage());
            Staff newStaff = staffRepository.save(staff);

            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            StaffResponse staffResponse = modelMapper.map(newStaff, StaffResponse.class);
            staffResponse.setBirthday(newStaff.getDob().getTime());
            return staffResponse;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Cập nhật thông tin cá nhân thất bại");
        }
    }

    @Override
    public CustomerResponse customerProfile(HttpServletRequest request) throws CustomException {
        try {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);

            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getEmailFromToken(jwt);
            Customer customer = customerRepository.findByEmail(email);


            if (customer == null)
                throw new CustomException("Token không hợp lệ");

            CustomerResponse customerResponse = modelMapper.map(customer, CustomerResponse.class);
            if (customer.getDob() != null) customerResponse.setBirthday(customer.getDob().getTime());
            return customerResponse;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            throw new CustomException("Lỗi: người dùng không hợp lệ hoặc không tồn tại");
        }
    }

    @Override
    public CustomerResponse customerUpdateProfile(CustomerUpdateRequest customerUpdateRequest, HttpServletRequest request) throws CustomException {
        //validate
        String matchNumber = "[0-9]+";
        if (customerUpdateRequest.getCardId() != null && !customerUpdateRequest.getCardId().equals("")) {
            if (!customerUpdateRequest.getCardId().matches(matchNumber))
                throw new CustomException("Số CMND phải là số");
            else if (customerUpdateRequest.getCardId().length() < 9 || customerUpdateRequest.getCardId().length() > 12)
                throw new CustomException("Số CMND phải gồm 9-12 số");
        }
        if (!customerUpdateRequest.getPhone().matches(matchNumber))
            throw new CustomException("Số điện thoại phải là số");
        if (customerUpdateRequest.getBirthday() >= System.currentTimeMillis())
            throw new CustomException("Ngày sinh phải trong quá khứ");

        //update
        try {
            String jwt = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getEmailFromToken(jwt);
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.getConfiguration()
                    .setMatchingStrategy(MatchingStrategies.STRICT);
            Customer customer = customerRepository.findByEmail(email);
            if (customer == null) throw new CustomException("Token không hợp lệ");

            customer.setName(customerUpdateRequest.getName());
            customer.setGender(customerUpdateRequest.isGender());
            customer.setAddress(customerUpdateRequest.getAddress());
            customer.setPhone(customerUpdateRequest.getPhone());
            customer.setCardId(customerUpdateRequest.getCardId());
            customer.setDob(new Date(customerUpdateRequest.getBirthday()));
            customer.setImage(customerUpdateRequest.getImage());
            Customer newCustomer = customerRepository.save(customer);
            CustomerResponse customerResponse = modelMapper.map(newCustomer, CustomerResponse.class);
            if (customer.getDob() != null) customerResponse.setBirthday(newCustomer.getDob().getTime());
            return customerResponse;
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            //e.printStackTrace();
            throw new CustomException("Cập nhật thông tin cá nhân thất bại");
        }
    }

    @Override
    public MessageResponse changePassword(String oldPass, String newPass,
                                          HttpServletRequest request) throws CustomException {
        try {
            String token = extractJwtFromRequest(request);
            String email = jwtTokenUtil.getEmailFromToken(token);
            String role = jwtTokenUtil.getRoleFromToken(token);
            if (email == null || email.trim().equals(""))
                throw new CustomException("Token không hợp lệ");

            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_SUPER_ADMIN")) {
                Staff staff = staffRepository.findByEmail(email);
                if (staff != null) {
                    if (!passwordEncoder.matches(oldPass, staff.getPass()))
                        throw new CustomException("Mật khẩu cũ không chính xác");
                    staff.setPass(passwordEncoder.encode(newPass));
                    staffRepository.save(staff);
                    return new MessageResponse("Đổi mật khẩu cho nhân viên: " + staff.getEmail() + " thành công");
                } else throw new CustomException("Không tìm thấy nhân viên hợp lệ");
            } else {
                Customer customer = customerRepository.findByEmail(email);
                if (customer != null) {
                    if (!passwordEncoder.matches(oldPass, customer.getPass()))
                        throw new CustomException("Mật khẩu cũ không chính xác");
                    customer.setPass(passwordEncoder.encode(newPass));
                    customerRepository.save(customer);
                    return new MessageResponse("Đổi mật khẩu khách hàng: " + customer.getEmail() + " thành công");
                } else throw new CustomException("Không tìm thấy người dùng hợp lệ");
            }
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        } catch (Exception e) {
            throw new CustomException("Đổi mật khẩu thất bại");
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
        Map<String, Object> expectedMap = new HashMap<>();
        for (Entry<String, Object> entry : claims.entrySet()) {
            expectedMap.put(entry.getKey(), entry.getValue());
        }
        return expectedMap;
    }
}