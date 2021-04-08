package com.phamthehuy.doan.helper;

import com.phamthehuy.doan.authentication.CustomJwtAuthenticationFilter;
import com.phamthehuy.doan.util.auth.JwtUtil;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.exception.BadRequestException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class Helper {
    final
    CustomerRepository customerRepository;
    final
    StaffRepository staffRepository;
    final
    JwtUtil jwtUtil;
    final
    CustomJwtAuthenticationFilter customJwtAuthenticationFilter;

    public Helper(CustomerRepository customerRepository, StaffRepository staffRepository, JwtUtil jwtUtil, CustomJwtAuthenticationFilter customJwtAuthenticationFilter) {
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.jwtUtil = jwtUtil;
        this.customJwtAuthenticationFilter = customJwtAuthenticationFilter;
    }

    public String getHostUrl(String url, String substring) {
        int index = url.indexOf(substring);
        return url.substring(0, index);
    }

    public String getBaseURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if ((serverPort != 80) && (serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        url.append(contextPath);
        if (url.toString().endsWith("/")) {
            url.append("/");
        }
        return url.toString();
    }

    public String getEmailFromRequest(HttpServletRequest request) throws BadRequestException {
        try {
            String token = customJwtAuthenticationFilter.extractJwtFromRequest(request);
            return jwtUtil.getEmailFromToken(token);
        } catch (Exception e) {
            throw new BadRequestException("Token bị thiếu, hoặc không hợp lệ");
        }
    }

    public String createToken(int numberOfCharacter) {
        //create token
        String token;
        do {
            token = randomAlphaNumeric(numberOfCharacter);
        } while (customerRepository.findByToken(token) != null
                || staffRepository.findByToken(token) != null);
        return token;
    }

    private final String alpha = "abcdefghijklmnopqrstuvwxyz"; // a-z
    private final String alphaUpperCase = alpha.toUpperCase(); // A-Z
    private final String digits = "0123456789"; // 0-9
    private final String ALPHA_NUMERIC = alpha + alphaUpperCase + digits;

    private Random generator = new Random();

    /**
     * Random string with a-zA-Z0-9, not included special characters
     */
    public String randomAlphaNumeric(int numberOfCharacter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfCharacter; i++) {
            int number = randomNumber(0, ALPHA_NUMERIC.length() - 1);
            char ch = ALPHA_NUMERIC.charAt(number);
            sb.append(ch);
        }
        return sb.toString();
    }

    public int randomNumber(int min, int max) {
        return generator.nextInt((max - min) + 1) + min;
    }

    public String listToText(List<String> list) {
        StringBuilder string = new StringBuilder();
        for (String s : list) {
            string.append(s).append("___");
        }
        return string.toString();
    }

    public List<String> textToList(String text) {
        String[] arr = text.split("___");
        return Arrays.asList(arr);
    }

    public Integer calculateDays(int number, String type, Date starTime) {
        Calendar start = Calendar.getInstance();
        start.setTime(starTime);
        long millisecond;
        if (type.trim().equals("week")) {
            Calendar end = Calendar.getInstance();
            end.setTime(starTime);
            end.add(Calendar.WEEK_OF_YEAR, number);
            millisecond = end.getTime().getTime() - start.getTime().getTime();
        } else if (type.trim().equals("month")) {
            Calendar end = Calendar.getInstance();
            end.setTime(starTime);
            end.add(Calendar.MONTH, number);
            millisecond = end.getTime().getTime() - start.getTime().getTime();
        } else
            return 0;
        return (int) (millisecond / (24 * 3600 * 1000));
    }

    public Date addDayForDate(Integer days, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}
