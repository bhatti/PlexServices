package com.plexobject.service.jetty;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseBuilder;

/**
 * This class replies object using http protocol
 * 
 * @author shahzad bhatti
 *
 */
public class HttpResponseBuilder extends AbstractResponseBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(HttpResponseBuilder.class);

    private final Request baseRequest;
    private final HttpServletResponse response;

    public HttpResponseBuilder(final CodecType codecType,
            final Request baseRequest, final HttpServletResponse response) {
        super(codecType);
        this.baseRequest = baseRequest;
        this.response = response;
    }

    public void addSessionId(String value) {
        properties.put(Constants.SESSION_ID, value);
        response.addCookie(new Cookie(Constants.SESSION_ID, value));
    }

    protected void doSend(String payload) {
        String location = (String) properties.get(Constants.LOCATION);
        if (location != null) {
            redirect(location);
            return;
        }
        try {
            response.setContentType(codecType.getContentType());
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

            response.getWriter().println(payload);
            baseRequest.setHandled(true);

        } catch (Exception e) {
            log.error("Failed to write " + payload + ", " + this, e);
        }
    }

    @Override
    public String toString() {
        return toString(baseRequest);
    }

    public static String toString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Method:" + request.getMethod());
        sb.append(", Path:" + request.getPathInfo());
        sb.append(", Host:" + request.getRemoteHost());
        for (Map.Entry<String, String[]> e : request.getParameterMap()
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
