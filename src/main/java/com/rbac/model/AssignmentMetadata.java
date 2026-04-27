package com.rbac.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("assignedBy не может быть пустым");
        }
        if (assignedAt == null || assignedAt.trim().isEmpty()) {
            throw new IllegalArgumentException("assignedAt не может быть пустым");
        }
    }
    
    public static AssignmentMetadata now(String assignedBy, String reason) {
        String now = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, now, reason);
    }
    
    public static AssignmentMetadata now(String assignedBy) {
        return now(assignedBy, null);
    }
    
    public String format() {
        if (reason == null || reason.trim().isEmpty()) {
            return String.format("Assigned by: %s at %s", assignedBy, assignedAt);
        } else {
            return String.format("Assigned by: %s at %s, reason: %s", assignedBy, assignedAt, reason);
        }
    }
    
    public static void main(String[] args) {
        AssignmentMetadata meta1 = AssignmentMetadata.now("avdeev_egor", "Need access to reports");
        System.out.println(meta1.format());
        
        AssignmentMetadata meta2 = AssignmentMetadata.now("simonyan_levon");
        System.out.println(meta2.format());
        
        try {
            AssignmentMetadata bad = new AssignmentMetadata("", "2024-01-01", "test");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}