package com.rbac.audit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncAuditLog {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final BlockingQueue<AuditEntry> queue;
    private final List<AuditEntry> entries;
    private final Thread workerThread;
    private final AtomicBoolean running;
    
    public record AuditEntry(String timestamp, String action, String performer, String target, String details) {}
    
    public AsyncAuditLog() {
        this.queue = new LinkedBlockingQueue<>();
        this.entries = new ArrayList<>();
        this.running = new AtomicBoolean(true);
        
        this.workerThread = new Thread(() -> {
            while (running.get() || !queue.isEmpty()) {
                try {
                    AuditEntry entry = queue.take();
                    entries.add(entry);
                    System.out.println("[ASYNC AUDIT] " + formatEntry(entry));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        workerThread.setDaemon(true);
        workerThread.start();
    }
    
    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        queue.offer(entry);
    }
    
    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }
    
    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equals(performer))
                .toList();
    }
    
    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equals(action))
                .toList();
    }
    
    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Лог пуст");
            return;
        }
        System.out.println("\n---------- АСИНХРОННЫЙ ЛОГ СОБЫТИЙ ----------");
        for (AuditEntry entry : entries) {
            System.out.println(formatEntry(entry));
        }
        System.out.println("----------------------------------------------\n");
    }
    
    public void saveToFile(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(filename))) {
            writer.println("TIMESTAMP|ACTION|PERFORMER|TARGET|DETAILS");
            for (AuditEntry entry : entries) {
                writer.printf("%s|%s|%s|%s|%s%n", 
                    entry.timestamp(), entry.action(), entry.performer(), entry.target(), entry.details());
            }
            System.out.println("Лог сохранен в " + filename);
        } catch (java.io.IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }
    
    public void shutdown() {
        running.set(false);
        workerThread.interrupt();
    }
    
    private String formatEntry(AuditEntry entry) {
        return String.format("[%s] %s | %s | %s | %s", 
            entry.timestamp(), entry.action(), entry.performer(), entry.target(), entry.details());
    }
}