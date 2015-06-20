package com.plexobject.http;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Response;

/**
 * This class sends response using implementation of http-response
 * 
 * @author shahzad bhatti
 *
 */
public class HttpResponseDispatcher extends AbstractResponseDispatcher {
    protected static final Logger log = Logger
            .getLogger(HttpResponseDispatcher.class);

    private final Handledable handledable;
    private final HttpResponse response;

    public HttpResponseDispatcher(final Handledable handledable,
            final HttpResponse response) {
        this.handledable = handledable;
        this.response = response;
    }

    @Override
    protected void doSend(Response reply, String payload) {
        String location = (String) reply
                .getStringProperty(HttpResponse.LOCATION);
        if (location != null) {
            redirect(location);
            return;
        }
        String sessionId = (String) reply
                .getStringProperty(Constants.SESSION_ID);
        if (sessionId != null) {
            response.addCookie(Constants.SESSION_ID, sessionId);
        }
        try {
            if (reply.getCodecType() != null) {
                String contentType = (String) reply
                        .getStringProperty(HttpResponse.CONTENT_TYPE);
                if (contentType == null) {
                    response.setContentType(reply.getCodecType()
                            .getContentType());
                }
            }
            response.setStatus(reply.getStatus());
            //
            for (Map.Entry<String, Object> e : reply.getProperties().entrySet()) {
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
