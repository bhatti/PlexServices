package com.plexobject.bugger.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.plexobject.bugger.model.User;

public class UserRepository {
    private final Map<String, User> memoryStorage = new HashMap<>();

    public User load(String username) {
        return memoryStorage.get(username);
    }

    public void save(User u) {
        memoryStorage.put(u.getUsername(), u);
    }

    public void delete(String username) {
        memoryStorage.remove(username);
    }

    public List<User> getAll() {
        return new ArrayList<>(memoryStorage.values());
    }

    public int count() {
        return memoryStorage.size();
    }
}
