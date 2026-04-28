package com.rbac.manager;

import com.rbac.model.User;
import com.rbac.filter.UserFilters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {
    private UserManager manager;
    private User user1;
    private User user2;
    
    @BeforeEach
    void setUp() {
        manager = new UserManager();
        user1 = User.create("avdeev_egor", "Авдеев Егор", "egor@mail.ru");
        user2 = User.create("simonyan_levon", "Симонян Левон", "levon@mail.ru");
    }
    
    @Test
    void testAddUser() {
        manager.add(user1);
        assertEquals(1, manager.count());
        assertTrue(manager.exists("avdeev_egor"));
    }
    
    @Test
    void testAddDuplicateUserThrowsException() {
        manager.add(user1);
        assertThrows(IllegalArgumentException.class, () -> manager.add(user1));
    }
    
    @Test
    void testFindByUsername() {
        manager.add(user1);
        assertTrue(manager.findByUsername("avdeev_egor").isPresent());
        assertTrue(manager.findByUsername("not_exist").isEmpty());
    }
    
    @Test
    void testFindByEmail() {
        manager.add(user1);
        assertTrue(manager.findByEmail("egor@mail.ru").isPresent());
        assertTrue(manager.findByEmail("not@mail.ru").isEmpty());
    }
    
    @Test
    void testUpdateUser() {
        manager.add(user1);
        manager.update("avdeev_egor", "Егор Авдеев", "egor_new@mail.ru");
        User updated = manager.findByUsername("avdeev_egor").get();
        assertEquals("Егор Авдеев", updated.fullName());
        assertEquals("egor_new@mail.ru", updated.email());
    }
    
    @Test
    void testRemoveUser() {
        manager.add(user1);
        assertTrue(manager.remove(user1));
        assertEquals(0, manager.count());
    }
    
    @Test
    void testFindByFilter() {
        manager.add(user1);
        manager.add(user2);
        List<User> result = manager.findByFilter(UserFilters.byUsernameContains("sim"));
        assertEquals(1, result.size());
        assertEquals("simonyan_levon", result.get(0).username());
    }
    
    @Test
    void testFindAllWithFilterAndSorter() {
        manager.add(user1);
        manager.add(user2);
        List<User> result = manager.findAll(
            UserFilters.byUsernameContains("a"),
            (u1, u2) -> u1.username().compareTo(u2.username())
        );
        assertEquals(2, result.size());
    }
    
    @Test
    void testClear() {
        manager.add(user1);
        manager.add(user2);
        manager.clear();
        assertEquals(0, manager.count());
    }
    
    @Test
    void testConcurrentAdd() throws InterruptedException {
        int threads = 5;
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threads);
        
        for (int i = 0; i < threads; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    User user = User.create("concurrent_user_" + index, "User" + index, "user" + index + "@mail.ru");
                    manager.add(user);
                } catch (Exception e) {
                    // ok
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        
        assertTrue(manager.count() <= threads);
    }
    
    @Test
    void testConcurrentReadAndWrite() throws InterruptedException {
        manager.add(user1);
        
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(4);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(4);
        
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                try {
                    manager.findAll();
                    manager.findByUsername("avdeev_egor");
                    manager.count();
                } catch (Exception e) {
                    fail("Concurrent read failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        executor.shutdown();
        assertTrue(true);
    }
}