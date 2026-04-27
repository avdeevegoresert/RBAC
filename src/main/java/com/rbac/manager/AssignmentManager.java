package com.rbac.manager;

import com.rbac.model.*;
import com.rbac.repository.Repository;
import com.rbac.filter.AssignmentFilter;
import java.util.*;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments;
    private final UserManager userManager;
    private final RoleManager roleManager;
    
    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.assignments = new HashMap<>();
        this.userManager = userManager;
        this.roleManager = roleManager;
    }
    
    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }
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
    }
    
    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }
        return assignments.remove(assignment.assignmentId()) != null;
    }
    
    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }
    
    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }
    
    @Override
    public int count() {
        return assignments.size();
    }
    
    @Override
    public void clear() {
        assignments.clear();
    }
    
    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }
    
    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }
    
    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }
    
    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }
    
    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }
    
    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }
    
    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
    }
    
    public boolean userHasPermission(User user, String permissionName, String resource) {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .filter(a -> a.user().equals(user))
                .anyMatch(a -> a.role().hasPermission(permissionName, resource));
    }
    
    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> permissions = new HashSet<>();
        assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .filter(a -> a.user().equals(user))
                .forEach(a -> permissions.addAll(a.role().getPermissions()));
        return permissions;
    }
    
    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }
        if (assignment instanceof PermanentAssignment) {
            ((PermanentAssignment) assignment).revoke();
        }
        assignments.remove(assignmentId);
    }
    
    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }
        if (!(assignment instanceof TemporaryAssignment)) {
            throw new IllegalArgumentException("Assignment is not temporary");
        }
        ((TemporaryAssignment) assignment).extend(newExpirationDate);
    }
}