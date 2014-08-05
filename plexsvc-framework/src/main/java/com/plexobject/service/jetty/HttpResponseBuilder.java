package com.plexobject.service.jetty;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.ResponseBuilder;

public class HttpResponseBuilder extends ResponseBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(HttpResponseBuilder.class);

    private final String contentType;
    private final CodecType codecType;
    private final Request baseRequest;
    private final HttpServletResponse response;

    public HttpResponseBuilder(final String contentType,
            final CodecType codecType, final Request baseRequest,
            final HttpServletResponse response) {
        this.contentType = contentType;
        this.codecType = codecType;
        this.baseRequest = baseRequest;
        this.response = response;
    }

    public void addSessionId(String value) {
        response.addCookie(new Cookie(Constants.SESSION_ID, value));
    }

    public final void send(Object payload) {
        String location = (String) properties.get(Constants.LOCATION);
        if (location != null) {
            redirect(location);
            return;
        }

        String sessionId = (String) properties.get(Constants.SESSION_ID);
        if (sessionId != null) {
            addSessionId(sessionId);
        }

        try {
            response.setContentType(contentType);
            if (response.getStatus() > 0) {
                response.setStatus(response.getStatus());
            } else {
                String status = (String) properties.get(Constants.STATUS);
                if (status != null) {
                    response.setStatus(Integer.parseInt(status));
                }

            }
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

            baseRequest.setHandled(true);
            String replyText = payload instanceof String ? (String) payload
                    : ObjectCodeFactory.getObjectCodec(codecType).encode(
                            payload);
            response.getWriter().println(replyText);
        } catch (Exception e) {
            log.error("Failed to write " + payload + ", " + this, e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Method:" + baseRequest.getMethod());
        sb.append(", Path:" + baseRequest.getPathInfo());
        sb.append(", Host:"
                + baseRequest.getRemoteInetSocketAddress().getHostName());
        for (Map.Entry<String, String[]> e : baseRequest.getParameterMap()
                .entrySet()) {
            sb.append(", " + e.getKey() + " -> " + e.getValue()[0]);

        }
        return sb.toString();
    }

    private void redirect(String location) {
        try {
            response.sendRedirect(location);
        } catch (IOException e) {
            throw new RuntimeException("Failed to redirect to " + location);
        }
    }
}
