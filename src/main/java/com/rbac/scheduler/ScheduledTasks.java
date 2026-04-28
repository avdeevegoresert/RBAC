package com.rbac.scheduler;

import com.rbac.manager.AssignmentManager;
import com.rbac.model.RoleAssignment;
import com.rbac.model.TemporaryAssignment;
import com.rbac.audit.AuditLog;
import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledTasks {
    private static ScheduledTasks instance;
    private final ScheduledExecutorService scheduler;
    private AssignmentManager assignmentManager;
    private AuditLog auditLog;
    private boolean running;
    
    private ScheduledTasks() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.running = false;
    }
    
    public static synchronized ScheduledTasks getInstance() {
        if (instance == null) {
            instance = new ScheduledTasks();
        }
        return instance;
    }
    
    public void start(AssignmentManager assignmentManager, AuditLog auditLog) {
        if (running) {
            System.out.println("Планировщик уже запущен");
            return;
        }
        
        this.assignmentManager = assignmentManager;
        this.auditLog = auditLog;
        this.running = true;
        
        // Задача 1: каждые 30 секунд проверять истекшие временные назначения
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkExpiredAssignments();
            } catch (Exception e) {
                System.err.println("Ошибка проверки истекших назначений: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.SECONDS);
        
        // Задача 2: каждые 60 секунд писать статистику в лог
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logStatistics();
            } catch (Exception e) {
                System.err.println("Ошибка логирования статистики: " + e.getMessage());
            }
        }, 10, 60, TimeUnit.SECONDS);
        
        if (auditLog != null) {
            auditLog.log("SCHEDULER_START", "system", "ScheduledTasks", "Планировщик задач запущен");
        }
        System.out.println("Планировщик задач запущен");
    }
    
    public void stop() {
        if (!running) return;
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        running = false;
        
        if (auditLog != null) {
            auditLog.log("SCHEDULER_STOP", "system", "ScheduledTasks", "Планировщик задач остановлен");
        }
        System.out.println("Планировщик задач остановлен");
    }
    
    private void checkExpiredAssignments() {
        if (assignmentManager == null) return;
        
        int expiredCount = 0;
        for (RoleAssignment assignment : assignmentManager.findAll()) {
            if (assignment instanceof TemporaryAssignment) {
                TemporaryAssignment temp = (TemporaryAssignment) assignment;
                if (temp.isActive() && temp.isExpired()) {
                    temp.revoke();
                    expiredCount++;
                    if (auditLog != null) {
                        auditLog.log("AUTO_EXPIRE", "scheduler", assignment.user().username(), 
                            "Истекло временное назначение роли " + assignment.role().getName());
                    }
                }
            }
        }
        
        if (expiredCount > 0 && auditLog != null) {
            auditLog.log("EXPIRED_CHECK", "scheduler", "system", 
                "Помечено истекших назначений: " + expiredCount);
        }
    }
    
    private void logStatistics() {
        if (assignmentManager == null || auditLog == null) return;
        
        int activeCount = assignmentManager.getActiveAssignments().size();
        int totalCount = assignmentManager.count();
        
        auditLog.log("STATS_REPORT", "scheduler", "system", 
            String.format("Активных назначений: %d/%d", activeCount, totalCount));
    }
    
    public boolean isRunning() {
        return running;
    }
}