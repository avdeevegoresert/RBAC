package com.rbac.report;

import com.rbac.manager.UserManager;
import com.rbac.manager.RoleManager;
import com.rbac.manager.AssignmentManager;
import com.rbac.model.User;
import com.rbac.model.Role;
import com.rbac.model.Permission;
import com.rbac.model.AssignmentMetadata;
import com.rbac.model.PermanentAssignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {
    
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private ReportGenerator reportGenerator;
    
    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        reportGenerator = new ReportGenerator();
        
        User user1 = User.create("avdeev_egor", "Авдеев Егор", "egor@mail.ru");
        User user2 = User.create("simonyan_levon", "Симонян Левон", "levon@mail.ru");
        userManager.add(user1);
        userManager.add(user2);
        
        Permission readUsers = new Permission("read", "users", "Чтение пользователей");
        Permission writeUsers = new Permission("write", "users", "Запись пользователей");
        
        Role admin = new Role("Admin", "Администратор");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        roleManager.add(admin);
        
        AssignmentMetadata meta = AssignmentMetadata.now("system", "Тест");
        PermanentAssignment assign = new PermanentAssignment(user1, admin, meta);
        assignmentManager.add(assign);
    }
    
    @Test
    void testGenerateUserReport() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("avdeev_egor"));
        assertTrue(report.contains("simonyan_levon"));
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("Всего пользователей: 2"));
    }
    
    @Test
    void testGenerateRoleReport() {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("1")); 
        assertTrue(report.contains("Всего ролей: 1"));
    }
    
    @Test
    void testGeneratePermissionMatrix() {
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(report.contains("users"));
        assertTrue(report.contains("READ"));
        assertTrue(report.contains("WRITE"));
    }
    
    @Test
    void testExportToFile() throws Exception {
        String report = "Тестовый отчет";
        String filename = "test_report.txt";
        reportGenerator.exportToFile(report, filename);
        
        File file = new File(filename);
        assertTrue(file.exists());
        file.delete();
    }
}