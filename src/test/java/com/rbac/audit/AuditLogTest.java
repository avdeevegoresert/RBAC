package com.rbac.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    
    private AuditLog auditLog;
    
    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }
    
    @Test
    void testLog() {
        auditLog.log("CREATE_USER", "админ", "avdeev_egor", "Создан пользователь avdeev_egor");
        assertEquals(1, auditLog.getAll().size());
    }
    
    @Test
    void testGetByPerformer() {
        auditLog.log("CREATE_USER", "админ", "avdeev_egor", "Создан пользователь");
        auditLog.log("DELETE_ROLE", "админ", "viewer", "Удалена роль");
        auditLog.log("ASSIGN_ROLE", "менеджер", "simonyan_levon", "Назначена роль");
        
        List<AuditLog.AuditEntry> byPerformer = auditLog.getByPerformer("админ");
        assertEquals(2, byPerformer.size());
    }
    
    @Test
    void testGetByAction() {
        auditLog.log("CREATE_USER", "админ", "avdeev_egor", "Создан пользователь");
        auditLog.log("CREATE_USER", "админ", "simonyan_levon", "Создан пользователь");
        auditLog.log("DELETE_USER", "админ", "beletskiy_d", "Удален пользователь");
        
        List<AuditLog.AuditEntry> byAction = auditLog.getByAction("CREATE_USER");
        assertEquals(2, byAction.size());
    }
    
    @Test
    void testPrintLog() {
        auditLog.log("CREATE_USER", "админ", "avdeev_egor", "Создан пользователь");
        auditLog.printLog();
        assertDoesNotThrow(() -> auditLog.printLog());
    }
    
    @Test
    void testSaveToFile() throws IOException {
        auditLog.log("CREATE_USER", "админ", "avdeev_egor", "Создан пользователь");
        String filename = "test_audit.log";
        auditLog.saveToFile(filename);
        
        File file = new File(filename);
        assertTrue(file.exists());
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String firstLine = reader.readLine();
        assertEquals("TIMESTAMP|ACTION|PERFORMER|TARGET|DETAILS", firstLine);
        reader.close();
        file.delete();
    }
}