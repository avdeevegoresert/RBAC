package com.rbac.executor;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class BackgroundExecutorTest {
    
    @Test
    void testSubmitTask() throws InterruptedException {
        AtomicBoolean executed = new AtomicBoolean(false);
        BackgroundExecutor.getInstance().submit(() -> {
            executed.set(true);
        });
        
        Thread.sleep(500);
        assertTrue(executed.get());
    }
    
    @Test
    void testSingleton() {
        BackgroundExecutor instance1 = BackgroundExecutor.getInstance();
        BackgroundExecutor instance2 = BackgroundExecutor.getInstance();
        assertSame(instance1, instance2);
    }
}