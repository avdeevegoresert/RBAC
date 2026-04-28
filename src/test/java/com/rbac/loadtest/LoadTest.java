package com.rbac.loadtest;

import com.rbac.system.RBACSystem;
import com.rbac.model.User;
import com.rbac.model.Role;
import com.rbac.model.Permission;
import com.rbac.model.AssignmentMetadata;
import com.rbac.model.PermanentAssignment;
import com.rbac.filter.UserFilters;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class LoadTest {
    
    @Test
    void testConcurrentUserOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();
        
        int threads = 10;
        int operationsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            String username = "user_" + threadId + "_" + j;
                            User user = User.create(username, "Test User " + username, username + "@test.com");
                            system.getUserManager().add(user);
                            successCount.incrementAndGet();
                            
                            var found = system.getUserManager().findByUsername(username);
                            assertTrue(found.isPresent());
                            
                            system.getUserManager().findByFilter(UserFilters.byUsernameContains("user_"));
                            
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long endTime = System.currentTimeMillis();
        
        System.out.println("\n--- НАГРУЗОЧНЫЙ ТЕСТ ---");
        System.out.println("Успешных операций: " + successCount.get());
        System.out.println("Ошибок: " + errorCount.get());
        System.out.println("Время выполнения: " + (endTime - startTime) + " ms");
        System.out.println("Пользователей в системе: " + system.getUserManager().count());
        System.out.println("----------------------------------\n");
        
        assertTrue(successCount.get() > 0);
        assertTrue(system.getUserManager().count() > 0);
    }
    
    @Test
    void testConcurrentRoleAndAssignment() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();
        
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger roleCount = new AtomicInteger(0);
        AtomicInteger assignCount = new AtomicInteger(0);
        
        Permission readPerm = new Permission("read", "test", "Test permission");
        
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    String roleName = "Role_" + threadId;
                    Role role = new Role(roleName, "Test role");
                    role.addPermission(readPerm);
                    system.getRoleManager().add(role);
                    roleCount.incrementAndGet();
                    
                    String username = "assign_user_" + threadId;
                    User user = User.create(username, "Test User", username + "@test.com");
                    system.getUserManager().add(user);
                    
                    AssignmentMetadata meta = AssignmentMetadata.now("system", "load test");
                    PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
                    system.getAssignmentManager().add(assignment);
                    assignCount.incrementAndGet();
                    
                } catch (Exception e) {
                    // допустимо при дубликатах
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        System.out.println("\n--- НАГРУЗОЧНЫЙ ТЕСТ (роли+назначения) ----");
        System.out.println("Создано ролей: " + roleCount.get());
        System.out.println("Создано назначений: " + assignCount.get());
        System.out.println("Всего ролей: " + system.getRoleManager().count());
        System.out.println("Всего назначений: " + system.getAssignmentManager().count());
        System.out.println("-----------------------------------------------\n");
        
        assertTrue(system.getRoleManager().count() > 0);
        assertTrue(system.getAssignmentManager().count() > 0);
    }
    
    @Test
    void testConcurrentReadOperations() throws InterruptedException {
        RBACSystem system = new RBACSystem();
        system.initialize();
        
        for (int i = 0; i < 20; i++) {
            User user = User.create("read_user_" + i, "Read User", "read" + i + "@test.com");
            system.getUserManager().add(user);
        }
        
        int threads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger readCount = new AtomicInteger(0);
        
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        system.getUserManager().findAll();
                        system.getUserManager().count();
                        system.getRoleManager().findAll();
                        system.getAssignmentManager().getActiveAssignments();
                        readCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Read error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        System.out.println("\n------ НАГРУЗОЧНЫЙ ТЕСТ (чтение) -----");
        System.out.println("Операций чтения: " + readCount.get());
        System.out.println("Пользователи: " + system.getUserManager().count());
        System.out.println("Роли: " + system.getRoleManager().count());
        System.out.println("-----------------\n");
        
        assertTrue(readCount.get() > 0);
    }
}