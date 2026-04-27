package com.rbac.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {
    private RBACSystem system;
    
    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
    }
    
    @Test
    void testInitializeCreatesData() {
        assertEquals(1, system.getUserManager().count());
        assertEquals(3, system.getRoleManager().count());
        assertEquals(1, system.getAssignmentManager().count());
    }
    
    @Test
    void testCurrentUser() {
        system.setCurrentUser("test_user");
        assertEquals("test_user", system.getCurrentUser());
    }
    
    @Test
    void testGenerateStatistics() {
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Users: 1"));
        assertTrue(stats.contains("Roles: 3"));
        assertTrue(stats.contains("Assignments: 1"));
    }
    
    @Test
    void testAdminExists() {
        assertTrue(system.getUserManager().exists("admin"));
    }
}