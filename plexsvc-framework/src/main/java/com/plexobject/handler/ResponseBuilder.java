package com.plexobject.handler;

import java.util.HashMap;
import java.util.Map;

public abstract class ResponseBuilder {
    protected int status;
    protected String contentType;
    protected final Map<String, Object> properties = new HashMap<>();

    public ResponseBuilder setStatus(int status) {
        this.status = status;
        return this;
    }

    public ResponseBuilder setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public ResponseBuilder setProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public abstract void send(Object payload);

    public abstract void addSessionId(String value);

}
