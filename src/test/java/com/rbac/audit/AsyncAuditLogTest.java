    package com.rbac.audit;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AsyncAuditLogTest {
    
    @Test
    void testAsyncLog() throws InterruptedException {
        AsyncAuditLog log = new AsyncAuditLog();
        log.log("TEST", "tester", "system", "Тестовое сообщение");
        
        Thread.sleep(200);
        assertEquals(1, log.getAll().size());
    }
    
    @Test
    void testGetByAction() throws InterruptedException {
        AsyncAuditLog log = new AsyncAuditLog();
        log.log("CREATE", "admin", "user1", "Создан пользователь");
        log.log("DELETE", "admin", "user2", "Удален пользователь");
        
        Thread.sleep(200);
        assertEquals(1, log.getByAction("CREATE").size());
        assertEquals(1, log.getByAction("DELETE").size());
    }
    
    @Test
    void testPrintLog() throws InterruptedException {
        AsyncAuditLog log = new AsyncAuditLog();
        log.log("TEST", "tester", "system", "test");
        Thread.sleep(200);
        
        assertDoesNotThrow(() -> log.printLog());
    }
}