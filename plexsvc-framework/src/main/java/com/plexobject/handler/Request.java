package com.plexobject.handler;

import java.util.Map;

public class Request {
    private final Map<String, Object> properties;
    private final long createdAt;
    private final String sessionId;
    private final String username;
    private final Object object;
    private final ResponseBuilder responseBuilder;

    public Request(final Map<String, Object> properties,
            final String sessionId, final String username, final Object object,
            final ResponseBuilder responseBuilder) {
        this.createdAt = System.currentTimeMillis();
        this.properties = properties;
        this.sessionId = sessionId;
        this.username = username;
        this.object = object;
        this.responseBuilder = responseBuilder;
    }

    @SuppressWarnings("unchecked")
    public <V> V getProperty(String name) {
        return (V) properties.get(name);
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject() {
        return (T) object;
    }

    public String getUsername() {
        return username;
    }

    public ResponseBuilder getResponseBuilder() {
        return responseBuilder;
    }

}
