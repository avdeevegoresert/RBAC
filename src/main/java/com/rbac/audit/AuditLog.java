package com.rbac.audit;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final List<AuditEntry> entries;
    
    public record AuditEntry(String timestamp, String action, String performer, String target, String details) {}
    
    public AuditLog() {
        this.entries = new ArrayList<>();
    }
    
    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
        System.out.println("[LOG] " + action + " performed by " + performer);
    }
    
    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }
    
    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equals(performer))
                .collect(Collectors.toList());
    }
    
    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equals(action))
                .collect(Collectors.toList());
    }
    
    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Лог пуст");
            return;
        }
        System.out.println("\n ЛОГ СОБЫТИЙ ");
        for (AuditEntry entry : entries) {
            System.out.printf("[%s] %s | %s | %s | %s%n",
                entry.timestamp(), entry.action(), entry.performer(), entry.target(), entry.details());
        }
        System.out.println("---------------------\n");
    }
    
    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("TIMESTAMP|ACTION|PERFORMER|TARGET|DETAILS");
            for (AuditEntry entry : entries) {
                writer.printf("%s|%s|%s|%s|%s%n",
                    entry.timestamp(), entry.action(), entry.performer(), entry.target(), entry.details());
            }
            System.out.println("Лог сохранен в " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения лога: " + e.getMessage());
        }
    }
}