package com.rbac.sort;

import com.rbac.model.Role;
import java.util.Comparator;

public class RoleSorters {
    
    public static Comparator<Role> byName() {
        return (r1, r2) -> r1.getName().compareTo(r2.getName());
    }
    
    public static Comparator<Role> byPermissionCount() {
        return (r1, r2) -> Integer.compare(r1.getPermissions().size(), r2.getPermissions().size());
    }
}