package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.plexobject.bugger.model.User;
import com.plexobject.predicate.Predicate;

public class UserRepository {
    private final Map<Long, User> memoryStorage = new ConcurrentHashMap<>();
    private Set<String> sessions = new HashSet<>();

    private AtomicLong nextId = new AtomicLong(1);

    public User load(Long id) {
        return memoryStorage.get(id);
    }

    public User save(User u) {
        if (u.getId() == null) {
            u.setId(nextId.incrementAndGet());
        }

        memoryStorage.put(u.getId(), u);
        return u;
    }

    public User getUserBySessionId(String sessionId) {
        if (sessionId != null && sessions.contains(sessionId)) {
            return memoryStorage.get(Long.valueOf(sessionId));
        }
        return null;
    }

    public String getSessionId(User user) {
        return String.valueOf(user.getId());
    }

    public User authenticate(String username, String password) {
        User user = get(username);
        if (user == null) {
            return null;
        }
        // comparing plain passwords for testing purpose onlys
        if (user.getPassword().equals(password)) {
            sessions.add(getSessionId(user));
            return user;
        }
        return null;
    }

    public boolean delete(Long id) {
        return memoryStorage.remove(id) != null;
    }

    public List<User> getAll(Predicate<User> predicate) {
        List<User> matched = new ArrayList<>();
        for (User u : memoryStorage.values()) {
            if (predicate == null || predicate.accept(u)) {
                matched.add(u);
            }
        }
        return matched;
    }

    public User get(String username) {
        for (User u : memoryStorage.values()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public int count() {
        return memoryStorage.size();
    }

    @Override
    public String toString() {
        return "UserRepository [sessions=" + sessions + "] "
                + System.identityHashCode(this);
    }

}
