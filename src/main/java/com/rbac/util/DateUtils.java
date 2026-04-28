package com.rbac.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
    
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }
    
    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }
    
    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }
    
    public static String addDays(String date, int days) {
        if (date == null) return null;
        LocalDate d = LocalDate.parse(date, DATE_FORMATTER);
        return d.plusDays(days).format(DATE_FORMATTER);
    }
    
    public static String formatRelativeTime(String date) {
        if (date == null) return "неизвестно";
        
        LocalDate target = LocalDate.parse(date, DATE_FORMATTER);
        LocalDate now = LocalDate.now();
        
        long days = ChronoUnit.DAYS.between(now, target);
        
        if (days < 0) {
            long absDays = -days;
            if (absDays == 1) return "1 день назад";
            if (absDays >= 2 && absDays <= 4) return absDays + " дня назад";
            return absDays + " дней назад";
        } else if (days > 0) {
            if (days == 1) return "через 1 день";
            if (days >= 2 && days <= 4) return "через " + days + " дня";
            return "через " + days + " дней";
        } else {
            return "сегодня";
        }
    }
    
    public static boolean isValidDateFormat(String date) {
        if (date == null) return false;
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}