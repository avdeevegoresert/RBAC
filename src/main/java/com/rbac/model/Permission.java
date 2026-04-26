package com.rbac.model;

public record Permission(String name, String resource, String description) {
    
    public Permission {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name не может быть пустым");
        }
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Name не должен содержать пробелов");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource не может быть пустым");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description не может быть пустым");
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
        boolean nameMatches = (namePattern == null) || name.contains(namePattern);
        boolean resourceMatches = (resourcePattern == null) || resource.contains(resourcePattern);
        return nameMatches && resourceMatches;
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
            Permission bad = new Permission("read write", "users", "test");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}