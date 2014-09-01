package com.plexobject.http;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDispatcher;

/**
 * This class sends response using implementation of http-response
 * 
 * @author shahzad bhatti
 *
 */
public class HttpResponseDispatcher extends AbstractResponseDispatcher {
    protected static final Logger log = LoggerFactory
            .getLogger(HttpResponseDispatcher.class);

    private final Handledable handledable;
    private final HttpResponse response;

    public HttpResponseDispatcher(final Handledable handledable,
            final HttpResponse response) {
        this.handledable = handledable;
        this.response = response;
    }

    public void addSessionId(String value) {
        properties.put(Constants.SESSION_ID, value);
        response.addCookie(Constants.SESSION_ID, value);
    }

    protected void doSend(String payload) {
        String location = (String) properties.get(Constants.LOCATION);

        if (location != null) {
            redirect(location);
            return;
        }
        try {
            if (codecType != null) {
                response.setContentType(codecType.getContentType());
            }
            response.setStatus(getStatus());
            //
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                Object value = e.getValue();
                if (value != null) {
                    if (value instanceof String) {
                        response.addHeader(e.getKey(), (String) value);
                    } else {
                        response.addHeader(e.getKey(), value.toString());
                    }
                }
            }
            response.send(payload);
            handledable.setHandled(true);
        } catch (Exception e) {
            log.error("Failed to write " + payload + ", " + this, e);
        }
    }

    private void redirect(String location) {
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            throw new RuntimeException("Failed to redirect to " + location);
        }
    }
}
