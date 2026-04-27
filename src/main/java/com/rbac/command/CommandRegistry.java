package com.rbac.command;

import com.rbac.system.RBACSystem;
import com.rbac.model.User;
import com.rbac.model.Role;
import com.rbac.model.Permission;
import com.rbac.model.AssignmentMetadata;
import com.rbac.model.PermanentAssignment;
import com.rbac.model.TemporaryAssignment;
import com.rbac.model.RoleAssignment;
import com.rbac.filter.UserFilters;
import com.rbac.filter.RoleFilters;
import com.rbac.filter.AssignmentFilters;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CommandRegistry {
    
    public static void registerAll(CommandParser parser, RBACSystem system) {
        
        
        
        // user-list
        parser.registerCommand("user-list", "Show all users", (scanner, sys) -> {
            List<User> users = sys.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("No users found");
                return;
            }
            System.out.println("\n" + String.format("%-20s %-25s %-30s", "USERNAME", "FULL NAME", "EMAIL"));
            System.out.println(String.format("%-20s %-25s %-30s", "--------", "---------", "-----"));
            for (User u : users) {
                System.out.println(String.format("%-20s %-25s %-30s", u.username(), u.fullName(), u.email()));
            }
            System.out.println();
        });
        
        // user-list with filter
        parser.registerCommand("user-list-filtered", "Show users with filter", (scanner, sys) -> {
            System.out.println("\nSearch filters:");
            System.out.println("1. Username contains");
            System.out.println("2. Email contains");
            System.out.println("3. Email domain");
            System.out.println("4. Full name contains");
            System.out.println("5. No filter (all users)");
            System.out.print("Choose filter: ");
            
            String choice = scanner.nextLine().trim();
            com.rbac.filter.UserFilter filter = null;
            
            if (!choice.equals("5")) {
                System.out.print("Value: ");
                String value = scanner.nextLine().trim();
                switch (choice) {
                    case "1": filter = UserFilters.byUsernameContains(value); break;
                    case "2": filter = UserFilters.byEmailContains(value); break;
                    case "3": filter = UserFilters.byEmailDomain(value); break;
                    case "4": filter = UserFilters.byFullNameContains(value); break;
                    default: System.out.println("Invalid choice"); return;
                }
            }
            
            List<User> results = (filter == null) ? sys.getUserManager().findAll() 
                                                   : sys.getUserManager().findByFilter(filter);
            
            if (results.isEmpty()) {
                System.out.println("No users found");
            } else {
                System.out.println("\n" + String.format("%-20s %-25s %-30s", "USERNAME", "FULL NAME", "EMAIL"));
                System.out.println(String.format("%-20s %-25s %-30s", "--------", "---------", "-----"));
                for (User u : results) {
                    System.out.println(String.format("%-20s %-25s %-30s", u.username(), u.fullName(), u.email()));
                }
                System.out.println("\nFound " + results.size() + " users");
            }
        });
        
        // user-create
        parser.registerCommand("user-create", "Create new user", (scanner, sys) -> {
            try {
                System.out.print("Username: ");
                String username = scanner.nextLine().trim();
                System.out.print("Full name: ");
                String fullName = scanner.nextLine().trim();
                System.out.print("Email: ");
                String email = scanner.nextLine().trim();
                
                User user = User.create(username, fullName, email);
                sys.getUserManager().add(user);
                System.out.println("User created: " + user.format());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
        
        // user-view
        parser.registerCommand("user-view", "View user details", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            System.out.println("\n" + String.format("%-20s %-20s %-30s", "USERNAME", "FULL NAME", "EMAIL"));
            System.out.println(String.format("%-20s %-20s %-30s", "--------", "---------", "-----"));
            System.out.println(String.format("%-20s %-20s %-30s", user.username(), user.fullName(), user.email()));
            
            var assignments = sys.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("\nNo roles assigned");
            } else {
                System.out.println("\nAssigned roles:");
                for (var a : assignments) {
                    System.out.println("  - " + a.role().getName() + " (" + a.assignmentType() + ", active: " + a.isActive() + ")");
                }
            }
            
            var permissions = sys.getAssignmentManager().getUserPermissions(user);
            if (!permissions.isEmpty()) {
                System.out.println("\nPermissions (grouped by resource):");
                Map<String, List<Permission>> byResource = permissions.stream()
                    .collect(Collectors.groupingBy(Permission::resource));
                for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                    System.out.println("  Resource: " + entry.getKey());
                    for (Permission p : entry.getValue()) {
                        System.out.println("    - " + p.name() + ": " + p.description());
                    }
                }
            }
            System.out.println();
        });
        
        // user-update
        parser.registerCommand("user-update", "Update user data", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            if (!sys.getUserManager().exists(username)) {
                System.out.println("User not found");
                return;
            }
            
            System.out.print("New full name: ");
            String newFullName = scanner.nextLine().trim();
            System.out.print("New email: ");
            String newEmail = scanner.nextLine().trim();
            
            try {
                sys.getUserManager().update(username, newFullName, newEmail);
                System.out.println("User updated");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
        
        // user-delete
        parser.registerCommand("user-delete", "Delete user", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            System.out.print("Confirm delete (yes/no): ");
            String confirm = scanner.nextLine().trim();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Cancelled");
                return;
            }
            
            var assignments = sys.getAssignmentManager().findByUser(user);
            for (var a : assignments) {
                sys.getAssignmentManager().remove(a);
            }
            sys.getUserManager().remove(user);
            System.out.println("User deleted");
        });
        
        // user-search
        parser.registerCommand("user-search", "Search users", (scanner, sys) -> {
            System.out.println("\nSearch filters:");
            System.out.println("1. Username contains");
            System.out.println("2. Email contains");
            System.out.println("3. Email domain");
            System.out.println("4. Full name contains");
            System.out.print("Choose filter: ");
            
            String choice = scanner.nextLine().trim();
            System.out.print("Value: ");
            String value = scanner.nextLine().trim();
            
            com.rbac.filter.UserFilter filter = null;
            switch (choice) {
                case "1": filter = UserFilters.byUsernameContains(value); break;
                case "2": filter = UserFilters.byEmailContains(value); break;
                case "3": filter = UserFilters.byEmailDomain(value); break;
                case "4": filter = UserFilters.byFullNameContains(value); break;
                default: System.out.println("Invalid choice"); return;
            }
            
            List<User> results = sys.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No users found");
            } else {
                System.out.println("\n" + String.format("%-20s %-25s %-30s", "USERNAME", "FULL NAME", "EMAIL"));
                System.out.println(String.format("%-20s %-25s %-30s", "--------", "---------", "-----"));
                for (User u : results) {
                    System.out.println(String.format("%-20s %-25s %-30s", u.username(), u.fullName(), u.email()));
                }
                System.out.println("\nFound " + results.size() + " users");
            }
        });
        
        
        // role-list
        parser.registerCommand("role-list", "Show all roles", (scanner, sys) -> {
            List<Role> roles = sys.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles found");
                return;
            }
            System.out.println("\n" + String.format("%-20s %-10s %-15s", "ROLE NAME", "PERMISSIONS", "ID"));
            System.out.println(String.format("%-20s %-10s %-15s", "---------", "-----------", "--"));
            for (Role r : roles) {
                String shortId = r.getId().length() > 8 ? r.getId().substring(0, 8) + "..." : r.getId();
                System.out.println(String.format("%-20s %-10d %-15s", r.getName(), r.getPermissions().size(), shortId));
            }
            System.out.println();
        });
        
        // role-create
        parser.registerCommand("role-create", "Create new role", (scanner, sys) -> {
            System.out.print("Role name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Description: ");
            String desc = scanner.nextLine().trim();
            
            Role role = new Role(name, desc);
            sys.getRoleManager().add(role);
            System.out.println("Role created: " + name);
            
            System.out.print("Add permissions now? (yes/no): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                boolean addMore = true;
                while (addMore) {
                    System.out.print("Permission name (READ/WRITE/DELETE): ");
                    String pName = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Resource: ");
                    String resource = scanner.nextLine().trim().toLowerCase();
                    System.out.print("Description: ");
                    String pDesc = scanner.nextLine().trim();
                    
                    try {
                        Permission p = new Permission(pName, resource, pDesc);
                        role.addPermission(p);
                        System.out.println("Permission added");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    
                    System.out.print("Add another? (yes/no): ");
                    addMore = scanner.nextLine().trim().equalsIgnoreCase("yes");
                }
            }
        });
        
        // role-view
        parser.registerCommand("role-view", "View role details", (scanner, sys) -> {
            System.out.print("Role name: ");
            String name = scanner.nextLine().trim();
            
            var roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Role not found");
                return;
            }
            System.out.println("\n" + roleOpt.get().format());
        });
        
        // role-update
        parser.registerCommand("role-update", "Update role", (scanner, sys) -> {
            System.out.print("Role name: ");
            String name = scanner.nextLine().trim();
            
            var roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Role not found");
                return;
            }
            Role role = roleOpt.get();
            
            System.out.print("New name (empty to keep): ");
            String newName = scanner.nextLine().trim();
            if (!newName.isEmpty()) {
                role.setName(newName);
            }
            
            System.out.print("New description (empty to keep): ");
            String newDesc = scanner.nextLine().trim();
            if (!newDesc.isEmpty()) {
                role.setDescription(newDesc);
            }
            System.out.println("Role updated");
        });
        
        // role-delete
        parser.registerCommand("role-delete", "Delete role", (scanner, sys) -> {
            System.out.print("Role name: ");
            String name = scanner.nextLine().trim();
            
            var roleOpt = sys.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("Role not found");
                return;
            }
            Role role = roleOpt.get();
            
            var assignments = sys.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("Warning: Role is assigned to:");
                for (var a : assignments) {
                    System.out.println("  - " + a.user().username());
                }
                System.out.print("Confirm delete (yes/no): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    System.out.println("Cancelled");
                    return;
                }
            }
            
            sys.getRoleManager().remove(role);
            System.out.println("Role deleted");
        });
        
        // role-add-permission
        parser.registerCommand("role-add-permission", "Add permission to role", (scanner, sys) -> {
            System.out.print("Role name: ");
            String roleName = scanner.nextLine().trim();
            
            var roleOpt = sys.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Role not found");
                return;
            }
            Role role = roleOpt.get();
            
            System.out.print("Permission name (READ/WRITE/DELETE): ");
            String pName = scanner.nextLine().trim().toUpperCase();
            System.out.print("Resource: ");
            String resource = scanner.nextLine().trim().toLowerCase();
            System.out.print("Description: ");
            String desc = scanner.nextLine().trim();
            
            try {
                Permission p = new Permission(pName, resource, desc);
                role.addPermission(p);
                System.out.println("Permission added to role " + roleName);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
        
        // role-remove-permission
        parser.registerCommand("role-remove-permission", "Remove permission from role", (scanner, sys) -> {
            System.out.print("Role name: ");
            String roleName = scanner.nextLine().trim();
            
            var roleOpt = sys.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Role not found");
                return;
            }
            Role role = roleOpt.get();
            
            var permissions = role.getPermissions();
            if (permissions.isEmpty()) {
                System.out.println("No permissions for this role");
                return;
            }
            
            System.out.println("\nPermissions:");
            List<Permission> permList = new ArrayList<>(permissions);
            for (int i = 0; i < permList.size(); i++) {
                System.out.println((i+1) + ". " + permList.get(i).format());
            }
            
            System.out.print("Choose permission to remove: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                return;
            }
            if (choice < 0 || choice >= permList.size()) {
                System.out.println("Invalid choice");
                return;
            }
            
            role.removePermission(permList.get(choice));
            System.out.println("Permission removed");
        });
        
        // role-search
        parser.registerCommand("role-search", "Search roles", (scanner, sys) -> {
            System.out.println("\nSearch filters:");
            System.out.println("1. Name contains");
            System.out.println("2. Has permission");
            System.out.println("3. Min permissions count");
            System.out.print("Choose filter: ");
            
            String choice = scanner.nextLine().trim();
            List<Role> results = null;
            
            switch (choice) {
                case "1":
                    System.out.print("Substring: ");
                    String substr = scanner.nextLine().trim();
                    results = sys.getRoleManager().findByFilter(RoleFilters.byNameContains(substr));
                    break;
                case "2":
                    System.out.print("Permission name: ");
                    String pName = scanner.nextLine().trim().toUpperCase();
                    System.out.print("Resource: ");
                    String resource = scanner.nextLine().trim().toLowerCase();
                    results = sys.getRoleManager().findRolesWithPermission(pName, resource);
                    break;
                case "3":
                    System.out.print("Min count: ");
                    int min = Integer.parseInt(scanner.nextLine().trim());
                    results = sys.getRoleManager().findByFilter(RoleFilters.hasAtLeastNPermissions(min));
                    break;
                default:
                    System.out.println("Invalid choice");
                    return;
            }
            
            if (results == null || results.isEmpty()) {
                System.out.println("No roles found");
            } else {
                System.out.println("\nFound " + results.size() + " roles:");
                for (Role r : results) {
                    System.out.println("  - " + r.getName() + " (permissions: " + r.getPermissions().size() + ")");
                }
            }
        });
        
        
        
        // assign-role
        parser.registerCommand("assign-role", "Assign role to user", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            List<Role> roles = sys.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles available");
                return;
            }
            
            System.out.println("\nAvailable roles:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.println((i+1) + ". " + roles.get(i).getName());
            }
            System.out.print("Choose role number: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                return;
            }
            if (choice < 0 || choice >= roles.size()) {
                System.out.println("Invalid choice");
                return;
            }
            Role role = roles.get(choice);
            
            System.out.print("Assignment type (permanent/temporary): ");
            String type = scanner.nextLine().trim().toLowerCase();
            
            System.out.print("Reason: ");
            String reason = scanner.nextLine().trim();
            AssignmentMetadata meta = AssignmentMetadata.now(sys.getCurrentUser(), reason);
            
            try {
                if (type.equals("permanent")) {
                    PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
                    sys.getAssignmentManager().add(assignment);
                } else {
                    System.out.print("Expiration date (YYYY-MM-DD): ");
                    String expires = scanner.nextLine().trim();
                    System.out.print("Auto renew? (yes/no): ");
                    boolean autoRenew = scanner.nextLine().trim().equalsIgnoreCase("yes");
                    TemporaryAssignment assignment = new TemporaryAssignment(user, role, meta, expires, autoRenew);
                    sys.getAssignmentManager().add(assignment);
                }
                System.out.println("Role assigned successfully");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
        
        // revoke-role
        parser.registerCommand("revoke-role", "Revoke role from user", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            var assignments = sys.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("No assignments for this user");
                return;
            }
            
            System.out.println("\nAssignments:");
            for (int i = 0; i < assignments.size(); i++) {
                var a = assignments.get(i);
                System.out.println((i+1) + ". " + a.role().getName() + " (" + a.assignmentType() + ", active: " + a.isActive() + ")");
            }
            System.out.print("Choose assignment to revoke: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                return;
            }
            if (choice < 0 || choice >= assignments.size()) {
                System.out.println("Invalid choice");
                return;
            }
            
            sys.getAssignmentManager().revokeAssignment(assignments.get(choice).assignmentId());
            System.out.println("Assignment revoked");
        });
        
        // assignment-list
        parser.registerCommand("assignment-list", "List all assignments", (scanner, sys) -> {
            var assignments = sys.getAssignmentManager().findAll();
            if (assignments.isEmpty()) {
                System.out.println("No assignments");
                return;
            }
            System.out.println("\n" + String.format("%-20s %-15s %-12s %-10s %-20s", "USERNAME", "ROLE", "TYPE", "STATUS", "ASSIGNED AT"));
            System.out.println(String.format("%-20s %-15s %-12s %-10s %-20s", "--------", "----", "----", "------", "-----------"));
            for (var a : assignments) {
                String status = a.isActive() ? "active" : "inactive";
                System.out.println(String.format("%-20s %-15s %-12s %-10s %-20s", 
                    a.user().username(), a.role().getName(), a.assignmentType(), status, a.metadata().assignedAt()));
            }
            System.out.println();
        });
        
        // assignment-list-user
        parser.registerCommand("assignment-list-user", "Show user assignments", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            var assignments = sys.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("No assignments for this user");
                return;
            }
            
            System.out.println("\nAssignments for " + username + ":");
            for (var a : assignments) {
                System.out.println("  Role: " + a.role().getName());
                System.out.println("    Type: " + a.assignmentType());
                System.out.println("    Active: " + a.isActive());
                System.out.println("    Assigned by: " + a.metadata().assignedBy());
                System.out.println("    Assigned at: " + a.metadata().assignedAt());
                if (a.metadata().reason() != null && !a.metadata().reason().isEmpty()) {
                    System.out.println("    Reason: " + a.metadata().reason());
                }
                if (a instanceof TemporaryAssignment) {
                    System.out.println("    Expires: " + ((TemporaryAssignment) a).getExpiresAt());
                }
            }
            System.out.println();
        });
        
        // assignment-list-role
        parser.registerCommand("assignment-list-role", "Show users with role", (scanner, sys) -> {
            System.out.print("Role name: ");
            String roleName = scanner.nextLine().trim();
            
            var roleOpt = sys.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Role not found");
                return;
            }
            Role role = roleOpt.get();
            
            var assignments = sys.getAssignmentManager().findByRole(role);
            if (assignments.isEmpty()) {
                System.out.println("No users with this role");
                return;
            }
            
            System.out.println("\nUsers with role " + roleName + ":");
            for (var a : assignments) {
                System.out.println("  " + a.user().username() + " (" + a.assignmentType() + ", active: " + a.isActive() + ")");
            }
            System.out.println();
        });
        
        // assignment-active
        parser.registerCommand("assignment-active", "Show active assignments", (scanner, sys) -> {
            var assignments = sys.getAssignmentManager().getActiveAssignments();
            if (assignments.isEmpty()) {
                System.out.println("No active assignments");
                return;
            }
            System.out.println("\n" + String.format("%-20s %-15s %-12s %-20s", "USERNAME", "ROLE", "TYPE", "ASSIGNED AT"));
            System.out.println(String.format("%-20s %-15s %-12s %-20s", "--------", "----", "----", "-----------"));
            for (var a : assignments) {
                System.out.println(String.format("%-20s %-15s %-12s %-20s", 
                    a.user().username(), a.role().getName(), a.assignmentType(), a.metadata().assignedAt()));
            }
            System.out.println();
        });
        
        // assignment-expired
        parser.registerCommand("assignment-expired", "Show expired assignments", (scanner, sys) -> {
            var assignments = sys.getAssignmentManager().findAll();
            boolean found = false;
            
            System.out.println("\nExpired assignments:");
            for (var a : assignments) {
                if (!a.isActive() && a instanceof TemporaryAssignment) {
                    System.out.println("  " + a.user().username() + " -> " + a.role().getName());
                    System.out.println("    Expired at: " + ((TemporaryAssignment) a).getExpiresAt());
                    found = true;
                }
            }
            if (!found) {
                System.out.println("No expired assignments");
            }
            System.out.println();
        });
        
        // assignment-extend
        parser.registerCommand("assignment-extend", "Extend temporary assignment", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            var assignments = sys.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("No assignments for this user");
                return;
            }
            
            System.out.println("\nTemporary assignments:");
            int idx = 1;
            List<RoleAssignment> tempAssignments = new ArrayList<>();
            for (var a : assignments) {
                if (a instanceof TemporaryAssignment) {
                    System.out.println(idx + ". " + a.role().getName() + " (expires: " + ((TemporaryAssignment) a).getExpiresAt() + ")");
                    tempAssignments.add(a);
                    idx++;
                }
            }
            
            if (tempAssignments.isEmpty()) {
                System.out.println("No temporary assignments found");
                return;
            }
            
            System.out.print("Choose assignment to extend: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim()) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice");
                return;
            }
            if (choice < 0 || choice >= tempAssignments.size()) {
                System.out.println("Invalid choice");
                return;
            }
            
            System.out.print("New expiration date (YYYY-MM-DD): ");
            String newDate = scanner.nextLine().trim();
            
            sys.getAssignmentManager().extendTemporaryAssignment(tempAssignments.get(choice).assignmentId(), newDate);
            System.out.println("Assignment extended to " + newDate);
        });
        
        // assignment-search
        parser.registerCommand("assignment-search", "Search assignments", (scanner, sys) -> {
            System.out.println("\nSearch filters:");
            System.out.println("1. By user");
            System.out.println("2. By role");
            System.out.println("3. By type (permanent/temporary)");
            System.out.println("4. By status (active/inactive)");
            System.out.println("5. Assigned after date");
            System.out.println("6. Expiring before date");
            System.out.print("Choose filter: ");
            
            String choice = scanner.nextLine().trim();
            com.rbac.filter.AssignmentFilter filter = null;
            
            switch (choice) {
                case "1":
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    var userOpt = sys.getUserManager().findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("User not found");
                        return;
                    }
                    filter = AssignmentFilters.byUser(userOpt.get());
                    break;
                case "2":
                    System.out.print("Role name: ");
                    String roleName = scanner.nextLine().trim();
                    var roleOpt = sys.getRoleManager().findByName(roleName);
                    if (roleOpt.isEmpty()) {
                        System.out.println("Role not found");
                        return;
                    }
                    filter = AssignmentFilters.byRole(roleOpt.get());
                    break;
                case "3":
                    System.out.print("Type (permanent/temporary): ");
                    String type = scanner.nextLine().trim().toUpperCase();
                    filter = AssignmentFilters.byType(type);
                    break;
                case "4":
                    System.out.print("Status (active/inactive): ");
                    String status = scanner.nextLine().trim().toLowerCase();
                    filter = status.equals("active") ? AssignmentFilters.activeOnly() : AssignmentFilters.inactiveOnly();
                    break;
                case "5":
                    System.out.print("Date (YYYY-MM-DD HH:MM:SS): ");
                    String date = scanner.nextLine().trim();
                    filter = AssignmentFilters.assignedAfter(date);
                    break;
                case "6":
                    System.out.print("Date (YYYY-MM-DD): ");
                    String expDate = scanner.nextLine().trim();
                    filter = AssignmentFilters.expiringBefore(expDate);
                    break;
                default:
                    System.out.println("Invalid choice");
                    return;
            }
            
            List<RoleAssignment> results = sys.getAssignmentManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("No assignments found");
            } else {
                System.out.println("\nFound " + results.size() + " assignments:");
                for (var a : results) {
                    System.out.println("  " + a.user().username() + " -> " + a.role().getName() + 
                        " [" + a.assignmentType() + ", active: " + a.isActive() + "]");
                }
            }
            System.out.println();
        });
        
        // ==================== PERMISSION COMMANDS ====================
        
        // permissions-user
        parser.registerCommand("permissions-user", "Show user permissions", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            var permissions = sys.getAssignmentManager().getUserPermissions(user);
            if (permissions.isEmpty()) {
                System.out.println("No permissions for this user");
                return;
            }
            
            System.out.println("\nPermissions for " + username + " (grouped by resource):");
            Map<String, List<Permission>> byResource = permissions.stream()
                .collect(Collectors.groupingBy(Permission::resource));
            for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                System.out.println("\nResource: " + entry.getKey());
                for (Permission p : entry.getValue()) {
                    System.out.println("  - " + p.name() + ": " + p.description());
                }
            }
            System.out.println();
        });
        
        // permissions-check
        parser.registerCommand("permissions-check", "Check user permission", (scanner, sys) -> {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();
            
            var userOpt = sys.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("User not found");
                return;
            }
            User user = userOpt.get();
            
            System.out.print("Permission name: ");
            String permName = scanner.nextLine().trim();
            System.out.print("Resource: ");
            String resource = scanner.nextLine().trim();
            
            boolean has = sys.getAssignmentManager().userHasPermission(user, permName, resource);
            
            System.out.println("\nResult: " + (has ? "GRANTED" : "DENIED"));
            
            if (has) {
                var assignments = sys.getAssignmentManager().findByUser(user);
                for (var a : assignments) {
                    if (a.isActive() && a.role().hasPermission(permName, resource)) {
                        System.out.println("  Granted by role: " + a.role().getName());
                        break;
                    }
                }
            }
            System.out.println();
        });
        
        
        parser.registerCommand("help", "Show this help", (scanner, sys) -> {
            parser.printHelp();
        });
        
        // stats
        parser.registerCommand("stats", "Show system statistics", (scanner, sys) -> {
            int userCount = sys.getUserManager().count();
            int roleCount = sys.getRoleManager().count();
            int assignmentCount = sys.getAssignmentManager().count();
            int activeCount = sys.getAssignmentManager().getActiveAssignments().size();
            int expiredCount = assignmentCount - activeCount;
            
            double avgRolesPerUser = userCount > 0 ? (double) assignmentCount / userCount : 0;
            
            Map<String, Integer> rolePopularity = new HashMap<>();
            for (var a : sys.getAssignmentManager().findAll()) {
                rolePopularity.merge(a.role().getName(), 1, Integer::sum);
            }
            
            List<Map.Entry<String, Integer>> topRoles = rolePopularity.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(3)
                .collect(Collectors.toList());
            
            System.out.println("\nRBAC SYSTEM STATISTICS");
            System.out.println("Users: " + userCount);
            System.out.println("Roles: " + roleCount);
            System.out.println("Assignments (total): " + assignmentCount);
            System.out.println("Assignments (active): " + activeCount);
            System.out.println("Assignments (expired): " + expiredCount);
            System.out.printf("Average roles per user: %.2f\n", avgRolesPerUser);
            
            System.out.println("\nTop 3 most popular roles:");
            if (topRoles.isEmpty()) {
                System.out.println("  No roles assigned");
            } else {
                for (int i = 0; i < topRoles.size(); i++) {
                    System.out.println("  " + (i+1) + ". " + topRoles.get(i).getKey() + " (" + topRoles.get(i).getValue() + " assignments)");
                }
            }
            System.out.println("------------------------\n");
        });
        
        // clear
        parser.registerCommand("clear", "Clear screen", (scanner, sys) -> {
            for (int i = 0; i < 50; i++) System.out.println();
        });
        
                // exit
        parser.registerCommand("exit", "Exit program", (scanner, sys) -> {
            System.out.print("Confirm exit (yes/no): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Goodbye!");
                System.exit(0);
            }
        });
        
        // save
        parser.registerCommand("save", "Save data to file", (scanner, sys) -> {
            System.out.print("Filename: ");
            String filename = scanner.nextLine().trim();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("[USERS]");
                for (User u : sys.getUserManager().findAll()) {
                    writer.println(u.username() + "|" + u.fullName() + "|" + u.email());
                }
                
                writer.println("[ROLES]");
                for (Role r : sys.getRoleManager().findAll()) {
                    writer.print(r.getId() + "|" + r.getName() + "|" + r.getDescription());
                    for (Permission p : r.getPermissions()) {
                        writer.print("|" + p.name() + ":" + p.resource() + ":" + p.description());
                    }
                    writer.println();
                }
                
                writer.println("[ASSIGNMENTS]");
                for (RoleAssignment a : sys.getAssignmentManager().findAll()) {
                    writer.print(a.assignmentId() + "|" + a.user().username() + "|" + a.role().getId());
                    writer.print("|" + a.metadata().assignedBy() + "|" + a.metadata().assignedAt());
                    if (a.metadata().reason() != null && !a.metadata().reason().isEmpty()) {
                        writer.print("|" + a.metadata().reason());
                    }
                    writer.print("|" + a.assignmentType());
                    if (a instanceof TemporaryAssignment) {
                        writer.print("|" + ((TemporaryAssignment) a).getExpiresAt());
                    }
                    writer.println();
                }
                
                System.out.println("Data saved to " + filename);
            } catch (IOException e) {
                System.out.println("Error saving: " + e.getMessage());
            }
        });
        
        // load
        parser.registerCommand("load", "Load data from file", (scanner, sys) -> {
            System.out.print("Filename: ");
            String filename = scanner.nextLine().trim();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                String section = "";
                
                sys.getUserManager().clear();
                sys.getRoleManager().clear();
                sys.getAssignmentManager().clear();
                
                while ((line = reader.readLine()) != null) {
                    if (line.equals("[USERS]")) {
                        section = "USERS";
                        continue;
                    } else if (line.equals("[ROLES]")) {
                        section = "ROLES";
                        continue;
                    } else if (line.equals("[ASSIGNMENTS]")) {
                        section = "ASSIGNMENTS";
                        continue;
                    }
                    
                    if (line.isEmpty()) continue;
                    
                    String[] parts = line.split("\\|");
                    
                    if (section.equals("USERS") && parts.length >= 3) {
                        try {
                            User u = User.create(parts[0], parts[1], parts[2]);
                            sys.getUserManager().add(u);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Skip user: " + e.getMessage());
                        }
                    }
                    else if (section.equals("ROLES") && parts.length >= 3) {
                        Role role = new Role(parts[1], parts[2]);
                        for (int i = 3; i < parts.length; i++) {
                            String[] permParts = parts[i].split(":");
                            if (permParts.length >= 3) {
                                try {
                                    Permission p = new Permission(permParts[0], permParts[1], permParts[2]);
                                    role.addPermission(p);
                                } catch (IllegalArgumentException e) {
                                    System.out.println("Skip permission: " + e.getMessage());
                                }
                            }
                        }
                        sys.getRoleManager().add(role);
                    }
                    else if (section.equals("ASSIGNMENTS") && parts.length >= 7) {
                        String userId = parts[1];
                        var userOpt = sys.getUserManager().findByUsername(userId);
                        String roleId = parts[2];
                        var roleOpt = sys.getRoleManager().findById(roleId);
                        
                        if (userOpt.isPresent() && roleOpt.isPresent()) {
                            String reason = parts.length > 5 && !parts[5].equals("null") ? parts[5] : null;
                            AssignmentMetadata meta = new AssignmentMetadata(parts[3], parts[4], reason);
                            String type = parts.length > 6 ? parts[6] : "PERMANENT";
                            
                            try {
                                if (type.equals("PERMANENT")) {
                                    PermanentAssignment a = new PermanentAssignment(userOpt.get(), roleOpt.get(), meta);
                                    sys.getAssignmentManager().add(a);
                                } else {
                                    String expires = parts.length > 7 ? parts[7] : "2025-12-31";
                                    TemporaryAssignment a = new TemporaryAssignment(userOpt.get(), roleOpt.get(), meta, expires, false);
                                    sys.getAssignmentManager().add(a);
                                }
                            } catch (IllegalArgumentException e) {
                                System.out.println("Skip assignment: " + e.getMessage());
                            }
                        }
                    }
                }
                
                System.out.println("Data loaded from " + filename);
                System.out.println("Users: " + sys.getUserManager().count());
                System.out.println("Roles: " + sys.getRoleManager().count());
                System.out.println("Assignments: " + sys.getAssignmentManager().count());
                
            } catch (IOException e) {
                System.out.println("Error loading: " + e.getMessage());
            }
        });
    }
}