package com.phamthehuy.doan.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHeper {
    static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("dd/MM/yyyy");

    public static Date toDate(String date, String... pattern) {
        try {
            if (pattern.length > 0) {
                DATE_FORMATER.applyPattern(pattern[0]);
            }
            if (date == null) {
                return DateHeper.now();
            }
            return DATE_FORMATER.parse(date);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Date now() {
        return new Date();
    }


}
