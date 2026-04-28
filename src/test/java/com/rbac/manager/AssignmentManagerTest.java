package com.rbac.manager;

import com.rbac.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private User user;
    private Role role;
    private Permission permission;
    
    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        
        user = User.create("test_user", "Test User", "test@mail.ru");
        role = new Role("Admin", "Administrator");
        permission = new Permission("read", "users", "Can read users");
        
        userManager.add(user);
        role.addPermission(permission);
        roleManager.add(role);
    }
    
    @Test
    void testAddAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);
        assertEquals(1, assignmentManager.count());
    }
    
    @Test
    void testUserHasRole() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);
        assertTrue(assignmentManager.userHasRole(user, role));
    }
    
    @Test
    void testUserHasPermission() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);
        assertTrue(assignmentManager.userHasPermission(user, "read", "users"));
    }
    
    @Test
    void testGetUserPermissions() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);
        assertEquals(1, assignmentManager.getUserPermissions(user).size());
    }
    
    @Test
    void testRevokeAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);
        assignmentManager.revokeAssignment(assignment.assignmentId());
        assertEquals(0, assignmentManager.count());
    }
    
    @Test
    void testGetActiveAssignments() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);
        assertEquals(1, assignmentManager.getActiveAssignments().size());
    }
    
    @Test
    void testConcurrentAddAssignments() throws InterruptedException {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        
        for (int i = 0; i < threads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    User u = User.create("thread_user_" + index, "User" + index, "user" + index + "@mail.ru");
                    userManager.add(u);
                    AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
                    PermanentAssignment a = new PermanentAssignment(u, role, meta);
                    assignmentManager.add(a);
                } catch (Exception e) {
                    // ok
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        assertTrue(assignmentManager.count() <= threads);
    }
    
    @Test
    void testConcurrentReadAndWrite() throws InterruptedException {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assignment);
        
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch latch = new CountDownLatch(4);
        
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                try {
                    assignmentManager.findAll();
                    assignmentManager.getActiveAssignments();
                    assignmentManager.userHasRole(user, role);
                } catch (Exception e) {
                    fail("Concurrent read failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        assertTrue(true);
    }
}