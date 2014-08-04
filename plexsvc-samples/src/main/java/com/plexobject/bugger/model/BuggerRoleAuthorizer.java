package com.plexobject.bugger.model;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.security.RoleAuthorizer;

public class BuggerRoleAuthorizer implements RoleAuthorizer {
    private final UserRepository userRepository;

    public BuggerRoleAuthorizer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean hasRole(Request request, String role) {
        String username = request.getProperty("username");
        // TODO session validation here
        if (username == null) {
            return false;
        }
        User user = userRepository.get(username);
        if (user == null) {
            return false;
        }
        return user.getRoles().contains(role);
    }
}
