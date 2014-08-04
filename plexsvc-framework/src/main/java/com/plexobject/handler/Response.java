package com.plexobject.handler;

import java.util.Map;

public class Response extends AbstractPayload {
    public Response(final Map<String, Object> properties, final Object payload) {
        super(properties, payload);
    }
}
