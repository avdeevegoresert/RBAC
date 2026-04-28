package com.rbac.util;

import java.util.regex.Pattern;

public class ValidationUtils {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    
    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidDate(String date) {
        if (date == null) return false;
        if (!DATE_PATTERN.matcher(date).matches()) return false;
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        if (month < 1 || month > 12) return false;
        if (day < 1 || day > 31) return false;
        if (month == 2 && day > 29) return false;
        if (day > 30 && (month == 4 || month == 6 || month == 9 || month == 11)) return false;
        return true;
    }
    
    public static String normalizeString(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }
    
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
    }
    
    public static void validateUsername(String username) {
        if (!isValidUsername(username)) {
            throw new IllegalArgumentException("Username должен содержать только латиницу, цифры и _, длина 3-20");
        }
    }
    
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email должен содержать @ и точку");
        }
    }
    
    public static void validateDate(String date) {
        if (!isValidDate(date)) {
            throw new IllegalArgumentException("Дата должна быть в формате YYYY-MM-DD");
        }
    }
}