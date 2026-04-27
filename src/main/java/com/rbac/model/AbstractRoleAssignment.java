package com.rbac.model;

import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;
    
    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }
    
    @Override
    public String assignmentId() {
        return assignmentId;
    }
    
    @Override
    public User user() {
        return user;
    }
    
    @Override
    public Role role() {
        return role;
    }
    
    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return assignmentId.equals(that.assignmentId);
    }
    
    @Override
    public int hashCode() {
        return assignmentId.hashCode();
    }
    
    public abstract boolean isActive();
    
    public abstract String assignmentType();
    
    public String summary() {
        String type = assignmentType();
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        String reason = metadata.reason();
        
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(type).append("] ");
        sb.append(role().getName()).append(" assigned to ").append(user().username());
        sb.append(" by ").append(metadata.assignedBy()).append(" at ").append(metadata.assignedAt());
        
        if (reason != null && !reason.isEmpty()) {
            sb.append("\nReason: ").append(reason);
        }
        
        sb.append("\nStatus: ").append(status);
        
        return sb.toString();
    }
}