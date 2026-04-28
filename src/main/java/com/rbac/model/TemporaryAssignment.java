package com.rbac.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;
    
    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }
    
    @Override
    public boolean isActive() {
        if (autoRenew) {
            return true;
        }
        LocalDate now = LocalDate.now();
        LocalDate expire = LocalDate.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE);
        return !now.isAfter(expire);
    }
    
    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }
    
    public void extend(String newExpirationDate) {
        this.expiresAt = newExpirationDate;
    }
    
    public boolean isExpired() {
        LocalDate now = LocalDate.now();
        LocalDate expire = LocalDate.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE);
        return now.isAfter(expire);
    }
    
    public String getTimeRemaining() {
        if (autoRenew) {
            return "Auto-renewal active";
        }
        LocalDate now = LocalDate.now();
        LocalDate expire = LocalDate.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE);
        long days = ChronoUnit.DAYS.between(now, expire);
        if (days < 0) {
            return "Expired";
        }
        return days + " days remaining";
    }
    
    public String getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isAutoRenew() {
        return autoRenew;
    }
    
    @Override
    public String summary() {
        String base = super.summary();
        if (!autoRenew) {
            base = base + "\nExpires at: " + expiresAt;
        } else {
            base = base + "\nAuto-renew: enabled";
        }
        return base;
    }
    
    public static void main(String[] args) {
        try {
            User u = new User("test_user", "Test User", "test@mail.ru");
            Role r = new Role("Tester", "Test role");
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "temp access");
            
            TemporaryAssignment temp = new TemporaryAssignment(u, r, meta, "2026-12-31", false);
            System.out.println(temp.summary());
            System.out.println("Days left: " + temp.getTimeRemaining());
            System.out.println("Is active: " + temp.isActive());
            
            temp.extend("2027-12-31");
            System.out.println("After extend: " + temp.getTimeRemaining());
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void revoke() {
    this.expiresAt = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}