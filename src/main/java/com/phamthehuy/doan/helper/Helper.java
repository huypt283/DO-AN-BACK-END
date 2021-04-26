package com.phamthehuy.doan.helper;

import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.StaffRepository;
import com.phamthehuy.doan.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Service
public class Helper {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StaffRepository staffRepository;

    private final String alpha = "abcdefghijklmnopqrstuvwxyz"; // a-z
    private final String alphaUpperCase = alpha.toUpperCase(); // A-Z
    private final String digits = "0123456789"; // 0-9
    private final String ALPHA_NUMERIC = alpha + alphaUpperCase + digits;
    private final Random generator = new Random();

    public String createUserToken(int numberOfCharacter) {
        String token;
        do {
            token = randomAlphaNumeric(numberOfCharacter);
        } while (customerRepository.findByToken(token) != null
                || staffRepository.findByToken(token) != null);
        return token;
    }

    public String createPaymentToken(int numberOfCharacter) {
        String token;
        do {
            token = randomAlphaNumeric(numberOfCharacter);
        } while (transactionRepository.findByToken(token) != null);
        return token;
    }

    /**
     * Random string with a-zA-Z0-9, not included special characters
     */
    private String randomAlphaNumeric(int numberOfCharacter) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfCharacter; i++) {
            int number = randomNumber(ALPHA_NUMERIC.length() - 1);
            char ch = ALPHA_NUMERIC.charAt(number);
            sb.append(ch);
        }
        return sb.toString();
    }

    private int randomNumber(int max) {
        return generator.nextInt((max) + 1);
    }

    public Integer calculateDays(int times, String type, Date starTime) {
        Calendar start = Calendar.getInstance();
        start.setTime(starTime);
        long millisecond;
        if (type.trim().equals("week")) {
            Calendar end = Calendar.getInstance();
            end.setTime(starTime);
            end.add(Calendar.WEEK_OF_YEAR, times);
            millisecond = end.getTime().getTime() - start.getTime().getTime();
        } else if (type.trim().equals("month")) {
            Calendar end = Calendar.getInstance();
            end.setTime(starTime);
            end.add(Calendar.MONTH, times);
            millisecond = end.getTime().getTime() - start.getTime().getTime();
        } else
            return 0;
        return (int) (millisecond / (24 * 3600 * 1000));
    }

    public Date addDayForDate(Integer days, Date date) {
        date = date == null || new Date().after(date) ? new Date() : date;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}
