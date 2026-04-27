package com.rbac.manager;

import com.rbac.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
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
}