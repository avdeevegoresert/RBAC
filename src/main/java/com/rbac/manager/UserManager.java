package com.rbac.manager;

import com.rbac.model.User;
import com.rbac.repository.Repository;
import com.rbac.filter.UserFilter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class UserManager implements Repository<User> {
    private final Map<String, User> users;
    private final ReentrantReadWriteLock lock;
    
    public UserManager() {
        this.users = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }
    
    @Override
    public void add(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        lock.writeLock().lock();
        try {
            if (users.containsKey(user.username())) {
                throw new IllegalArgumentException("User with username " + user.username() + " already exists");
            }
            users.put(user.username(), user);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean remove(User user) {
        if (user == null) {
            return false;
        }
        lock.writeLock().lock();
        try {
            return users.remove(user.username()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public Optional<User> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(users.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public List<User> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(users.values());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return users.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            users.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Optional<User> findByUsername(String username) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(users.get(username));
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public Optional<User> findByEmail(String email) {
        lock.readLock().lock();
        try {
            return users.values().stream()
                    .filter(u -> u.email().equals(email))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<User> findByFilter(UserFilter filter) {
        lock.readLock().lock();
        try {
            return users.values().stream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        lock.readLock().lock();
        try {
            return users.values().stream()
                    .filter(filter::test)
                    .sorted(sorter)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean exists(String username) {
        lock.readLock().lock();
        try {
            return users.containsKey(username);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public void update(String username, String newFullName, String newEmail) {
        lock.writeLock().lock();
        try {
            User old = users.get(username);
            if (old == null) {
                throw new IllegalArgumentException("User not found: " + username);
            }
            User updated = User.create(username, newFullName, newEmail);
            users.put(username, updated);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public List<User> findByFilterParallel(UserFilter filter) {
        lock.readLock().lock();
        try {
            return users.values().parallelStream()
                    .filter(filter::test)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserManager that = (UserManager) o;
        return Objects.equals(users, that.users);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
    
      
}