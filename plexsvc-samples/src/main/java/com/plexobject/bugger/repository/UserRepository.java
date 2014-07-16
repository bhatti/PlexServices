package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plexobject.bugger.model.User;
import com.plexobject.predicate.Predicate;

public class UserRepository {
    private final Map<String, User> memoryStorage = new HashMap<>();

    public User load(String username) {
        return memoryStorage.get(username);
    }

    public User save(User u) {
        memoryStorage.put(u.getUsername(), u);
        return u;
    }

    public void delete(String username) {
        memoryStorage.remove(username);
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

    public int count() {
        return memoryStorage.size();
    }
}
