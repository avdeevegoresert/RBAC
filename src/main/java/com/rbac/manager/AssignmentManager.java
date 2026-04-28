package com.rbac.manager;

import com.rbac.model.*;
import com.rbac.repository.Repository;
import com.rbac.filter.AssignmentFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments;
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final ReentrantReadWriteLock lock;
    
    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignments = new ConcurrentHashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
        this.lock = new ReentrantReadWriteLock();
    }
    
    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }
        lock.writeLock().lock();
        try {
            if (assignments.containsKey(assignment.assignmentId())) {
                throw new IllegalArgumentException("Assignment already exists");
            }
            if (!userManager.exists(assignment.user().username())) {
                throw new IllegalArgumentException("User not found: " + assignment.user().username());
            }
            if (!roleManager.exists(assignment.role().getName())) {
                throw new IllegalArgumentException("Role not found: " + assignment.role().getName());
            }
            if (userHasRole(assignment.user(), assignment.role())) {
                throw new IllegalArgumentException("User already has this role assigned");
            }
            assignments.put(assignment.assignmentId(), assignment);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        lock.writeLock().lock();
        try {
            return assignments.remove(assignment.assignmentId()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Optional<RoleAssignment> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(assignments.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public List<RoleAssignment> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(assignments.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return assignments.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            assignments.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public List<RoleAssignment> findByUser(User user) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.user().equals(user))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<RoleAssignment> findByRole(Role role) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.role().equals(role))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(filter::test)
                    .sorted(sorter)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<RoleAssignment> getActiveAssignments() {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<RoleAssignment> getExpiredAssignments() {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(a -> !a.isActive())
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean userHasRole(User user, Role role) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean userHasPermission(User user, String permissionName, String resource) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(RoleAssignment::isActive)
                    .filter(a -> a.user().equals(user))
                    .anyMatch(a -> a.role().hasPermission(permissionName, resource));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Set<Permission> getUserPermissions(User user) {
        lock.readLock().lock();
        try {
            Set<Permission> permissions = new HashSet<>();
            assignments.values().stream()
                    .filter(RoleAssignment::isActive)
                    .filter(a -> a.user().equals(user))
                    .forEach(a -> permissions.addAll(a.role().getPermissions()));
            return permissions;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void revokeAssignment(String assignmentId) {
        lock.writeLock().lock();
        try {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new IllegalArgumentException("Assignment not found: " + assignmentId);
            }
            if (assignment instanceof PermanentAssignment) {
                ((PermanentAssignment) assignment).revoke();
            }
            assignments.remove(assignmentId);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        lock.writeLock().lock();
        try {
            RoleAssignment assignment = assignments.get(assignmentId);
            if (assignment == null) {
                throw new IllegalArgumentException("Assignment not found: " + assignmentId);
            }
            if (!(assignment instanceof TemporaryAssignment)) {
                throw new IllegalArgumentException("Assignment is not temporary");
            }
            ((TemporaryAssignment) assignment).extend(newExpirationDate);
        } finally {
            lock.writeLock().unlock();
        }
    }
}