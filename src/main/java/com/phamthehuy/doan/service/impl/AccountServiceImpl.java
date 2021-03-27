package com.phamthehuy.doan.service.impl;

import com.phamthehuy.doan.authentication.CustomUserDetailsService;
import com.phamthehuy.doan.authentication.JwtUtil;
import com.phamthehuy.doan.dao.CustomerRepository;
import com.phamthehuy.doan.dao.StaffRepository;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.dto.input.LoginDTO;
import com.phamthehuy.doan.model.dto.input.ResetPasswordDTO;
import com.phamthehuy.doan.model.dto.input.SignupDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.entity.Customer;
import com.phamthehuy.doan.model.entity.Staff;
import com.phamthehuy.doan.service.AccountService;
import com.phamthehuy.doan.util.MailSender;
import io.jsonwebtoken.impl.DefaultClaims;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

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

    public AccountServiceImpl(PasswordEncoder passwordEncoder, CustomerRepository customerRepository, MailSender mailSender, StaffRepository staffRepository, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtTokenUtil) {
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.mailSender = mailSender;
        this.staffRepository = staffRepository;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public Message customerSignup(SignupDTO signupDTO, HttpServletRequest request) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        if (customerRepository.findByEmail(signupDTO.getEmail()) != null
                || staffRepository.findByEmail(signupDTO.getEmail()) != null)
            return new Message("Email is already in use");

        //create token
        String token;
        while (true) {
            token = randomAlphaNumeric(30);
            if (customerRepository.findByToken(token) == null) break;
        }

        //create customer
        Customer customer = modelMapper.map(signupDTO, Customer.class);
        customer.setAccountBalance(0);
        customer.setPass(passwordEncoder.encode(signupDTO.getPass()));
        customer.setToken(token);
        customerRepository.save(customer);

        //send mail
        mailSender.send(
                signupDTO.getEmail(),
                "Xác nhận địa chỉ email",
                "<h2>Xác nhận địa chỉ email</h2>" +
                        "Click vào đường link sau để xác nhận email và kích hoạt tài khoản của bạn:<br/>" +
                        Helper.getHostUrl(request.getRequestURL().toString(), "/sign-up") + "/confirm?token-customer=" + token
                        + "&email=" + signupDTO.getEmail()
        );
        return new Message("Please check your mail to confirm");
    }

    //về sau chuyển thành void, return redirect
    @Override
    public Message confirmEmail(String token, String email) {
        Customer customer = customerRepository.findByToken(token);
        if (customer != null) {
            if (!customer.getEmail().equals(email)) return new Message("Email is not correct");
            customer.setEnabled(true);
            customerRepository.save(customer);

            //nen lam redirect
            return new Message("Confirm email successfully");
        } else return new Message("Confirm failed");
    }

    @Override
    public Map<String, String> login(LoginDTO loginDTO) throws Exception {
        Map<String, String> returnMap = new HashMap<>();

        if (customerRepository.findByEmail(loginDTO.getEmail()) == null ||
                !customerRepository.findByEmail(loginDTO.getEmail()).getEnabled()) {
            if(staffRepository.findByEmail(loginDTO.getEmail()) == null){
                returnMap.put("mess", "Email is not activated or account does not exist");
                return returnMap;
            }
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(), loginDTO.getPass()));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());

        final String token = jwtTokenUtil.generateToken(userDetails);

        String role;
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
            role = "SUPER_ADMIN";
        else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
            role = "ADMIN";
        else role = "CUSTOMER";


        returnMap.put("role", role);
        returnMap.put("token", token);

        return returnMap;
    }

    @Override
    public Map<String, String> refreshtoken(HttpServletRequest request) throws Exception {
        // From the HttpRequest get the claims
        DefaultClaims claims = (DefaultClaims) request.getAttribute("claims");

        Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
        String token = jwtTokenUtil.doGenerateToken(expectedMap, expectedMap.get("sub").toString());
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("token", token);
        return returnMap;
    }

    @Override
    public Message forgotPassword(String email) {
        Staff staff = null;
        staff=staffRepository.findByEmail(email);
        Customer customer = null;
        if (staff == null) customer = customerRepository.findByEmail(email);

        String token;

        if (staff != null) {
            while (true) {
                token = randomAlphaNumeric(31);
                if (staffRepository.findByToken(token) == null) break;
            }
            staff.setToken(token);
            staffRepository.save(staff);
        } else if (customer != null) {
            if (!customer.getEnabled()) return new Message("Email is not activated");
            while (true) {
                token = randomAlphaNumeric(31);
                if (customerRepository.findByToken(token) == null) break;
            }
            customer.setToken(token);
            customerRepository.save(customer);
        } else {
            return new Message("Email not found");
        }
        //send mail
        mailSender.send(
                email,
                "Quên mật khẩu",
                "<h2>Quên mật khẩu, làm mới mật khẩu</h2>" +
                        "Click vào đường link sau để tạo mới mật khẩu của bạn:<br/>" +
                        "dia chi frontend" + "/renew-password?token=" + token
                        + "&email=" + email
        );
        return new Message("Successfully, please check mail to next step");
    }

    @Override
    public Message resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Staff staff = null;
        Customer customer = null;
        staff = staffRepository.findByToken(resetPasswordDTO.getToken());
        if (staff == null) customer = customerRepository.findByToken(resetPasswordDTO.getToken());
        if (staff != null) {
            if (!staff.getEmail().equals(resetPasswordDTO.getEmail())) return new Message("Email is not correct");
            staff.setPass(passwordEncoder.encode(resetPasswordDTO.getPassword()));
            staff.setToken(null);
            staffRepository.save(staff);
            return new Message("Refresh password successfully");
        } else if (customer != null) {
            if (!customer.getEmail().equals(resetPasswordDTO.getEmail())) return new Message("Email is not correct");
            customer.setPass(passwordEncoder.encode(resetPasswordDTO.getPassword()));
            customer.setToken(null);
            customerRepository.save(customer);
            return new Message("Refresh password successfully");
        } else return new Message("Refresh password failed");
    }


    public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        for (Entry<String, Object> entry : claims.entrySet()) {
            expectedMap.put(entry.getKey(), entry.getValue());
        }
        return expectedMap;
    }

    private static final String alpha = "abcdefghijklmnopqrstuvwxyz"; // a-z
    private static final String alphaUpperCase = alpha.toUpperCase(); // A-Z
    private static final String digits = "0123456789"; // 0-9
    private static final String ALPHA_NUMERIC = alpha + alphaUpperCase + digits;

    private static Random generator = new Random();

    /**
     * Random string with a-zA-Z0-9, not included special characters
     */
    public String randomAlphaNumeric(int numberOfCharactor) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfCharactor; i++) {
            int number = randomNumber(0, ALPHA_NUMERIC.length() - 1);
            char ch = ALPHA_NUMERIC.charAt(number);
            sb.append(ch);
        }
        return sb.toString();
    }

    public static int randomNumber(int min, int max) {
        return generator.nextInt((max - min) + 1) + min;
    }
}
