package com.plexobject.bugger.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.http.HttpResponse;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;

public class BuggerRoleAuthorizer implements RoleAuthorizer {
    private final Logger log = LoggerFactory.getLogger(getClass());

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
            throw new AuthException(HttpResponse.SC_UNAUTHORIZED,
                    request.getSessionId(), "failed to validate session-id:"
                            + sessionId + ", request " + request);
        }
        for (String role : roles) {
            if (!user.getRoles().contains(role)) {
                throw new AuthException(HttpResponse.SC_UNAUTHORIZED,
                        request.getSessionId(), "failed to match role");
            }
        }
    }
}
