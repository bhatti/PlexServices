package com.plexobject.handler;

import java.util.Map;

public class Request extends AbstractPayload {
    private final ResponseBuilder responseBuilder;

    public Request(final Map<String, Object> properties, final Object payload,
            final ResponseBuilder responseBuilder) {
        super(properties, payload);
        this.responseBuilder = responseBuilder;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResponseBuilder> T getResponseBuilder() {
        return (T) responseBuilder;
    }

}
