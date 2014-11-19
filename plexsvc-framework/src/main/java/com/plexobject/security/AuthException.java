package com.plexobject.security;

import com.plexobject.domain.Redirectable;
import com.plexobject.domain.Statusable;
import com.plexobject.http.HttpResponse;

public class AuthException extends RuntimeException implements Redirectable,
        Statusable {
    private static final long serialVersionUID = 1L;
    private final String sessionId;
    private String location;

    public AuthException(String sessionId, String location, String message) {
        super(message);
        this.sessionId = sessionId;
        this.location = location;
    }

    public AuthException(String sessionId, String message) {
        super(message);
        this.sessionId = sessionId;
    }

    @Override
    public int getStatus() {
        return HttpResponse.SC_UNAUTHORIZED;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public String getSessionId() {
        return sessionId;
    }
}
