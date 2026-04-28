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
        userManager.add(user1);
        
        Permission readUsers = new Permission("read", "users", "Чтение пользователей");
        
        Role admin = new Role("Admin", "Администратор");
        admin.addPermission(readUsers);
        roleManager.add(admin);
        
        AssignmentMetadata meta = AssignmentMetadata.now("system", "Тест");
        PermanentAssignment assign = new PermanentAssignment(user1, admin, meta);
        assignmentManager.add(assign);
    }
    
    @Test
    void testGenerateUserReport() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("avdeev_egor"));
        assertTrue(report.contains("Admin"));
    }
    
    @Test
    void testGenerateRoleReport() {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("Всего ролей: 1"));
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

    @Test
    void testGenerateUserReportParallel() {
        String report = reportGenerator.generateUserReportParallel(userManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("parallel"));
        assertTrue(report.contains("avdeev_egor"));
    }
    
    @Test
    void testGeneratePermissionMatrixParallel() {
        String report = reportGenerator.generatePermissionMatrixParallel(userManager, assignmentManager);
        assertNotNull(report);
        assertTrue(report.contains("parallel"));
        assertTrue(report.contains("users"));
    }
}