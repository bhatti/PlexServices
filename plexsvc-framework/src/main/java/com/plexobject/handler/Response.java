package com.plexobject.handler;

import java.util.Map;

/**
 * This class encapsulates response
 * 
 * @author shahzad bhatti
 *
 */
public class Response extends AbstractPayload {
    public Response(final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload) {
        super(properties, headers, payload);
    }
}
