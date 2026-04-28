package com.rbac.manager;

import com.rbac.model.Role;
import com.rbac.model.Permission;
import com.rbac.repository.Repository;
import com.rbac.filter.RoleFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById;
    private final Map<String, Role> rolesByName;
    private final ReentrantReadWriteLock lock;
    
    public RoleManager() {
        this.rolesById = new ConcurrentHashMap<>();
        this.rolesByName = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }
    
    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        lock.writeLock().lock();
        try {
            if (rolesByName.containsKey(role.getName())) {
                throw new IllegalArgumentException("Role with name " + role.getName() + " already exists");
            }
            rolesById.put(role.getId(), role);
            rolesByName.put(role.getName(), role);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean remove(Role role) {
        if (role == null) {
            return false;
        }
        lock.writeLock().lock();
        try {
            Role removed = rolesById.remove(role.getId());
            if (removed != null) {
                rolesByName.remove(removed.getName());
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Optional<Role> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public List<Role> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return rolesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            rolesById.clear();
            rolesByName.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Optional<Role> findByName(String name) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesByName.get(name));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<Role> findByFilter(RoleFilter filter) {
        lock.readLock().lock();
        try {
            return rolesById.values().stream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        lock.readLock().lock();
        try {
            return rolesById.values().stream()
                    .filter(filter::test)
                    .sorted(sorter)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean exists(String name) {
        lock.readLock().lock();
        try {
            return rolesByName.containsKey(name);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void addPermissionToRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException("Role not found: " + roleName);
            }
            role.addPermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void removePermissionFromRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalArgumentException("Role not found: " + roleName);
            }
            role.removePermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        lock.readLock().lock();
        try {
            return rolesById.values().stream()
                    .filter(role -> role.hasPermission(permissionName, resource))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
}