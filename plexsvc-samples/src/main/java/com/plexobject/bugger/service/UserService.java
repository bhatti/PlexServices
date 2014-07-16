package com.plexobject.bugger.service;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.security.RolesAllowed;

public class UserService {
    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RolesAllowed("Employee")
    public User create(String username) {
        return userRepository.load(username);
    }

    @RolesAllowed("Administrator")
    public User addRole(String username, String role) {
        User user = userRepository.load(username);
        user.addRole(role);
        return userRepository.save(user);
    }

    @RolesAllowed("Administrator")
    public User removeRole(String username, String role) {
        User user = userRepository.load(username);
        user.removeRole(role);
        return userRepository.save(user);
    }

    @RolesAllowed("Administrator")
    public User create(User user) {
        return userRepository.save(user);
    }

    @RolesAllowed("Administrator")
    public User edit(User user) {
        return userRepository.save(user);
    }
}
