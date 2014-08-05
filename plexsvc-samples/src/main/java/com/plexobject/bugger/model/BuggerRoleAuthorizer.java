package com.plexobject.bugger.model;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.Constants;
import com.plexobject.handler.Request;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;

public class BuggerRoleAuthorizer implements RoleAuthorizer {
    private final UserRepository userRepository;

    public BuggerRoleAuthorizer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void authorize(Request request, String[] roles) throws AuthException {
        if (roles == null || roles.length == 0 || roles[0].equals("")) {
            return;
        }
        String sessionId = request.getSessionId();
        User user = userRepository.getUserBySessionId(sessionId);
        if (user == null) {
            throw new AuthException(Constants.SC_UNAUTHORIZED,
                    request.getSessionId(), request.getRemoteAddress(),
                    "failed to validate session-id");
        }
        for (String role : roles) {
            if (!user.getRoles().contains(role)) {
                throw new AuthException(Constants.SC_UNAUTHORIZED,
                        request.getSessionId(), request.getRemoteAddress(),
                        "failed to match role");
            }
        }
    }
}
