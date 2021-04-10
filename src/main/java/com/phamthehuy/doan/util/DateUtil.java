package com.phamthehuy.doan.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("dd/MM/yyyy");

    public static Date toDate(String date, String... pattern) {
        try {
            if (pattern.length > 0) {
                DATE_FORMATER.applyPattern(pattern[0]);
            }
            if (date == null) {
                return new Date();
            }
            return DATE_FORMATER.parse(date);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
