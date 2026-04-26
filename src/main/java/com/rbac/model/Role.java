package com.rbac.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final String id;
    private String name;
    private String description;
    private Set<Permission> permissions;
    
    public Role(String name, String description) {
        this.id = "role_" + UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }
    
    public Role(String name, String description, Set<Permission> permissions) {
        this.id = "role_" + UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>(permissions);
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void addPermission(Permission permission) {
        permissions.add(permission);
    }
    
    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }
    
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }
    
    public boolean hasPermission(String name, String resource) {
        for (Permission p : permissions) {
            if (p.name().equalsIgnoreCase(name) && p.resource().equalsIgnoreCase(resource)) {
                return true;
            }
        }
        return false;
    }
    
    public Set<Permission> getPermissions() {
        return Set.copyOf(permissions);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id.equals(role.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "Role{id='" + id + "', name='" + name + "', desc='" + description + "', perms=" + permissions.size() + "}";
    }
    
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions (").append(permissions.size()).append("):\n");
        for (Permission p : permissions) {
            sb.append(" - ").append(p.format()).append("\n");
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        Permission p1 = new Permission("read", "users", "Can read users");
        Permission p2 = new Permission("write", "users", "Can write users");
        
        Role admin = new Role("Admin", "Full access");
        admin.addPermission(p1);
        admin.addPermission(p2);
        
        System.out.println(admin.format());
        System.out.println(admin.hasPermission("read", "users"));
    }
}