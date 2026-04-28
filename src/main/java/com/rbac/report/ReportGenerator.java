package com.rbac.report;

import com.rbac.manager.UserManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.AssignmentManager;
import com.rbac.model.User;
import com.rbac.model.Role;
import com.rbac.model.Permission;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class ReportGenerator {
    
    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n---------- ОТЧЕТ ПО ПОЛЬЗОВАТЕЛЯМ ----------\n");
        sb.append(String.format("%-20s %-25s %-30s %-20s\n", "ЛОГИН", "ФИО", "EMAIL", "РОЛИ"));
        sb.append(String.format("%-20s %-25s %-30s %-20s\n", "-----", "---", "-----", "-----"));
        
        for (User user : userManager.findAll()) {
            var assignments = assignmentManager.findByUser(user);
            StringBuilder roles = new StringBuilder();
            for (var a : assignments) {
                if (roles.length() > 0) roles.append(", ");
                roles.append(a.role().getName());
                if (!a.isActive()) roles.append("(неактивна)");
            }
            String rolesStr = roles.length() == 0 ? "нет ролей" : roles.toString();
            sb.append(String.format("%-20s %-25s %-30s %-20s\n", 
                user.username(), user.fullName(), user.email(), rolesStr));
        }
        
        sb.append("\nВсего пользователей: " + userManager.count() + "\n");
        sb.append("--------------------------------------------------\n");
        return sb.toString();
    }
    
    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n---------- ОТЧЕТ ПО РОЛЯМ ----------\n");
        sb.append(String.format("%-20s %-15s %-20s\n", "РОЛЬ", "ПОЛЬЗОВАТЕЛЕЙ", "ОПИСАНИЕ"));
        sb.append(String.format("%-20s %-15s %-20s\n", "----", "-------------", "--------"));
        
        for (Role role : roleManager.findAll()) {
            int userCount = assignmentManager.findByRole(role).size();
            sb.append(String.format("%-20s %-15d %-20s\n", 
                role.getName(), userCount, role.getDescription()));
        }
        
        sb.append("\nВсего ролей: " + roleManager.count() + "\n");
        sb.append("-------------------------------------\n");
        return sb.toString();
    }
    
    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n---------- МАТРИЦА ПРАВ ----------\n");
        
        Set<String> allResources = new HashSet<>();
        Map<String, Map<String, Set<String>>> userPermissions = new HashMap<>();
        
        for (User user : userManager.findAll()) {
            Map<String, Set<String>> permsByResource = new HashMap<>();
            var permissions = assignmentManager.getUserPermissions(user);
            for (Permission p : permissions) {
                allResources.add(p.resource());
                permsByResource.computeIfAbsent(p.resource(), k -> new HashSet<>()).add(p.name());
            }
            userPermissions.put(user.username(), permsByResource);
        }
        
        sb.append(String.format("%-15s", "ПОЛЬЗОВАТЕЛЬ"));
        for (String resource : allResources) {
            sb.append(String.format("%-12s", resource));
        }
        sb.append("\n");
        
        for (int i = 0; i < 15 + allResources.size() * 12; i++) sb.append("-");
        sb.append("\n");
        
        for (User user : userManager.findAll()) {
            sb.append(String.format("%-15s", user.username()));
            for (String resource : allResources) {
                var perms = userPermissions.getOrDefault(user.username(), new HashMap<>());
                String rights = perms.getOrDefault(resource, new HashSet<>()).toString();
                rights = rights.replace("[", "").replace("]", "");
                if (rights.isEmpty()) rights = "-";
                sb.append(String.format("%-12s", rights.length() > 10 ? rights.substring(0, 7) + "..." : rights));
            }
            sb.append("\n");
        }
        
        sb.append("-------------------------------------\n");
        return sb.toString();
    }
    
    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("Отчет сохранен в файл: " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения отчета: " + e.getMessage());
        }
    }
}