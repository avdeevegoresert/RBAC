package com.rbac.model;

import com.rbac.util.ValidationUtils;

public record Permission(String name, String resource, String description) {
    
    public Permission {
        ValidationUtils.requireNonEmpty(name, "Name");
        ValidationUtils.requireNonEmpty(resource, "Resource");
        ValidationUtils.requireNonEmpty(description, "Description");
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Name не должен содержать пробелов");
        }
        name = name.toUpperCase();
        resource = resource.toLowerCase();
    }
    
    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }
    
    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern == null && resourcePattern == null) {
            return true;
        }
        boolean nameMatch = (namePattern == null) || name.contains(namePattern);
        boolean resourceMatch = (resourcePattern == null) || resource.contains(resourcePattern);
        return nameMatch && resourceMatch;
    }
    
    public static void main(String[] args) {
        try {
            Permission p1 = new Permission("read", "users", "Can read user data");
            System.out.println(p1.format());
            Permission p2 = new Permission("WRITE", "REPORTS", "Can modify reports");
            System.out.println(p2.format());
            System.out.println(p1.matches("READ", "users"));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        
        try {
            Permission p3 = new Permission("read write", "users", "test");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}