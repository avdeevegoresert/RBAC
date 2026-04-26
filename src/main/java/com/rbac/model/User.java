package com.rbac.model;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {
    
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");
    
    public User {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username не может быть пустым");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("FullName не может быть пустым");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username должен содержать только латиницу, цифры и _, длина 3-20");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email должен содержать @ и точку");
        }
    }
    
    public static User create(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }
    
    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
    
    public static void main(String[] args) {
        try {
            User u1 = User.create("avdeev_egor", "Авдеев Егор", "egor@mail.ru");
            System.out.println(u1.format());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        
        try {
            User u2 = User.create("ab", "Симонян Левон", "levon@mail.ru");
            System.out.println(u2.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
        
        try {
            User u3 = User.create("mamarasulov", "Мамарасулов Абдулазиз", "azizmail.ru");
            System.out.println(u3.format());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}