package com.rbac.scheduler;

import com.rbac.manager.UserManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.AssignmentManager;
import com.rbac.audit.AuditLog;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledTasksTest {
    
    @Test
    void testSingleton() {
        ScheduledTasks instance1 = ScheduledTasks.getInstance();
        ScheduledTasks instance2 = ScheduledTasks.getInstance();
        assertSame(instance1, instance2);
    }
    
    @Test
    void testStartStop() {
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager(userManager, roleManager);
        AuditLog auditLog = new AuditLog();
        
        ScheduledTasks scheduler = ScheduledTasks.getInstance();
        scheduler.start(assignmentManager, auditLog);
        assertTrue(scheduler.isRunning());
        
        scheduler.stop();
        assertFalse(scheduler.isRunning());
    }
}