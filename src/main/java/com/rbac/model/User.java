package com.rbac.model;

import com.rbac.util.ValidationUtils;

public record User(String username, String fullName, String email) {
    
    public User {
        ValidationUtils.requireNonEmpty(username, "Username");
        ValidationUtils.requireNonEmpty(fullName, "FullName");
        ValidationUtils.requireNonEmpty(email, "Email");
        ValidationUtils.validateUsername(username);
        ValidationUtils.validateEmail(email);
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