package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.plexobject.bugger.model.User;
import com.plexobject.predicate.Predicate;

public class UserRepository {
    private final Map<Long, User> memoryStorage = new HashMap<>();
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

}
