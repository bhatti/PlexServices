package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.plexobject.bugger.model.User;
import com.plexobject.predicate.Predicate;

public class UserRepository {
    private final Map<Long, User> memoryStorage = new HashMap<Long, User>() {
        private static final long serialVersionUID = 1L;
        {
            put(1L, new User(1L, "alex", "pass", "alex@plexobject.com",
                    "Employee"));
            put(2L, new User(2L, "jeff", "pass", "jeff@plexobject.com",
                    "Employee", "Manager"));
            put(3L, new User(3L, "scott", "pass", "scott@plexobject.com",
                    "Employee", "Manager", "Administrator"));
            put(4L, new User(4L, "erica", "pass", "erica@plexobject.com",
                    "Employee"));
        }
    };
    private AtomicLong nextId = new AtomicLong(memoryStorage.size());

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

    public void delete(Long id) {
        memoryStorage.remove(id);
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
