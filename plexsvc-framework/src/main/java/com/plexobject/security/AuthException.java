package com.plexobject.security;

public class AuthException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int status;
    private final String sessionId;
    private final String remoteAddress;
    private String location;

    public AuthException(int status, String sessionId, String remoteAddress,
            String location, String message) {
        super(message);
        this.status = status;
        this.remoteAddress = remoteAddress;
        this.sessionId = sessionId;
        this.location = location;
    }

    public AuthException(int status, String sessionId, String remoteAddress,
            String message) {
        super(message);
        this.status = status;
        this.remoteAddress = remoteAddress;
        this.sessionId = sessionId;
    }

    public int getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getSessionId() {
        return sessionId;
    }
}
