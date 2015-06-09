package com.plexobject.bugger.model;

import org.apache.log4j.Logger;


import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;

public class BuggerRoleAuthorizer implements RoleAuthorizer {
    private final Logger log = Logger.getLogger(getClass());

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
            log.info("could not authenticate " + sessionId + " -- " + request);
            throw new AuthException(request.getSessionId(),
                    "failed to validate session-id:" + sessionId + ", request "
                            + request);
        }
        for (String role : roles) {
            if (!user.getRoles().contains(role)) {
                throw new AuthException(request.getSessionId(),
                        "failed to match role");
            }
        }
    }
}
