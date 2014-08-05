package com.plexobject.handler;

import java.util.Map;

public class Request extends AbstractPayload {
    private final String sessionId;
    private final String remoteAddress;
    private final ResponseBuilder responseBuilder;

    public Request(final Map<String, Object> properties, final Object payload,
            final String sessionId, final String remoteAddress,
            final ResponseBuilder responseBuilder) {
        super(properties, payload);
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.responseBuilder = responseBuilder;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResponseBuilder> T getResponseBuilder() {
        return (T) responseBuilder;
    }

}