package com.rbac.manager;

import com.rbac.model.Role;
import com.rbac.model.Permission;
import com.rbac.filter.RoleFilters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {
    private RoleManager manager;
    private Role adminRole;
    private Role viewerRole;
    private Permission readPerm;
    private Permission writePerm;
    
    @BeforeEach
    void setUp() {
        manager = new RoleManager();
        readPerm = new Permission("read", "users", "Can read users");
        writePerm = new Permission("write", "users", "Can write users");
        
        adminRole = new Role("Admin", "Full access");
        viewerRole = new Role("Viewer", "Read only");
        
        adminRole.addPermission(readPerm);
        adminRole.addPermission(writePerm);
        viewerRole.addPermission(readPerm);
    }
    
    @Test
    void testAddRole() {
        manager.add(adminRole);
        assertEquals(1, manager.count());
        assertTrue(manager.exists("Admin"));
    }
    
    @Test
    void testAddDuplicateRoleThrowsException() {
        manager.add(adminRole);
        assertThrows(IllegalArgumentException.class, () -> manager.add(adminRole));
    }
    
    @Test
    void testFindByName() {
        manager.add(adminRole);
        assertTrue(manager.findByName("Admin").isPresent());
        assertTrue(manager.findByName("NotExist").isEmpty());
    }
    
    @Test
    void testAddPermissionToRole() {
        manager.add(viewerRole);
        Permission deletePerm = new Permission("delete", "users", "Can delete users");
        manager.addPermissionToRole("Viewer", deletePerm);
        assertTrue(viewerRole.hasPermission(deletePerm));
    }
    
    @Test
    void testFindRolesWithPermission() {
        manager.add(adminRole);
        manager.add(viewerRole);
        List<Role> result = manager.findRolesWithPermission("read", "users");
        assertEquals(2, result.size());
    }
    
    @Test
    void testRemoveRole() {
        manager.add(adminRole);
        assertTrue(manager.remove(adminRole));
        assertEquals(0, manager.count());
    }
    
    @Test
    void testFindByFilter() {
        manager.add(adminRole);
        manager.add(viewerRole);
        List<Role> result = manager.findByFilter(RoleFilters.byNameContains("Admin"));
        assertEquals(1, result.size());
        assertEquals("Admin", result.get(0).getName());
    }
}