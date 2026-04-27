package com.rbac.system;

import com.rbac.manager.UserManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.AssignmentManager;
import com.rbac.model.User;
import com.rbac.model.Role;
import com.rbac.model.Permission;
import com.rbac.model.AssignmentMetadata;
import com.rbac.model.PermanentAssignment;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;
    
    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
    }
    
    public UserManager getUserManager() {
        return userManager;
    }
    
    public RoleManager getRoleManager() {
        return roleManager;
    }
    
    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }
    
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }
    
    public String getCurrentUser() {
        return currentUser;
    }
    
    public void initialize() {
        Permission readUsers = new Permission("read", "users", "Can view users");
        Permission writeUsers = new Permission("write", "users", "Can edit users");
        Permission deleteUsers = new Permission("delete", "users", "Can delete users");
        Permission readReports = new Permission("read", "reports", "Can view reports");
        Permission writeReports = new Permission("write", "reports", "Can edit reports");
        
        Role adminRole = new Role("Admin", "Full system access");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readReports);
        adminRole.addPermission(writeReports);
        
        Role managerRole = new Role("Manager", "Can manage users and view reports");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readReports);
        
        Role viewerRole = new Role("Viewer", "Read only access");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readReports);
        
        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);
        
        User admin = User.create("admin", "System Administrator", "admin@rbac.com");
        userManager.add(admin);
        
        AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial admin setup");
        PermanentAssignment assignment = new PermanentAssignment(admin, adminRole, meta);
        assignmentManager.add(assignment);
        
        setCurrentUser("admin");
    }
    
    public String generateStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("RBAC SYSTEM STATISTICS\n");
        sb.append("Users: ").append(userManager.count()).append("\n");
        sb.append("Roles: ").append(roleManager.count()).append("\n");
        sb.append("Assignments: ").append(assignmentManager.count()).append("\n");
        sb.append("Active assignments: ").append(assignmentManager.getActiveAssignments().size()).append("\n");
        sb.append("-------------------------------");
        return sb.toString();
    }
}