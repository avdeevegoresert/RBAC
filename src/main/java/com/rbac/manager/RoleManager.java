package com.rbac.manager;

import com.rbac.model.Role;
import com.rbac.model.Permission;
import com.rbac.repository.Repository;
import com.rbac.filter.RoleFilter;
import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById;
    private final Map<String, Role> rolesByName;
    
    public RoleManager() {
        this.rolesById = new HashMap<>();
        this.rolesByName = new HashMap<>();
    }
    
    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (rolesByName.containsKey(role.getName())) {
            throw new IllegalArgumentException("Role with name " + role.getName() + " already exists");
        }
        rolesById.put(role.getId(), role);
        rolesByName.put(role.getName(), role);
    }
    
    @Override
    public boolean remove(Role role) {
        if (role == null) {
            return false;
        }
        Role removed = rolesById.remove(role.getId());
        if (removed != null) {
            rolesByName.remove(removed.getName());
            return true;
        }
        return false;
    }
    
    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }
    
    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }
    
    @Override
    public int count() {
        return rolesById.size();
    }
    
    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }
    
    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }
    
    public List<Role> findByFilter(RoleFilter filter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }
    
    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }
    
    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }
    
    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        role.addPermission(permission);
    }
    
    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        role.removePermission(permission);
    }
    
    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }
}   