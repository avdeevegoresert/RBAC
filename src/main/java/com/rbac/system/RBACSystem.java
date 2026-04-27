package com.rbac.system;

import com.rbac.manager.UserManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.AssignmentManager;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    
    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
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
}