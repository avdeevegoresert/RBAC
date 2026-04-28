package com.rbac.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BackgroundExecutor {
    private static BackgroundExecutor instance;
    private final ExecutorService executor;
    
    private BackgroundExecutor() {
        this.executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized BackgroundExecutor getInstance() {
        if (instance == null) {
            instance = new BackgroundExecutor();
        }
        return instance;
    }
    
    public void submit(Runnable task) {
        executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("Ошибка в фоновой задаче: " + e.getMessage());
            }
        });
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}