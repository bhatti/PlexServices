package com.plexobject.service.jetty;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.service.ErrorResponse;
import com.plexobject.service.ServiceConfig;

public class HttpResponseBuilder extends ResponseBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(HttpResponseBuilder.class);

    private final ServiceConfig config;
    private final Request baseRequest;
    private final HttpServletResponse response;

    public HttpResponseBuilder(final ServiceConfig config,
            final Request baseRequest, final HttpServletResponse response) {
        this.config = config;
        this.baseRequest = baseRequest;
        this.response = response;
    }

    public final void sendSuccess(Object payload) {
        try {
            response.setContentType(config.contentType());
            response.setStatus(status);
            for (Map.Entry<String, Object> e : properties.entrySet()) {
                response.addHeader(e.getKey(), e.getValue().toString());
            }
            baseRequest.setHandled(true);
            String replyText = ObjectCodeFactory.getObjectCodec(config.codec())
                    .encode(payload);
            response.getWriter().println(replyText);
        } catch (Exception e) {
            log.error("Failed to write " + payload + ", " + this, e);
        }
    }

    @Override
    public void sendError(Exception e) {
        response.setContentType(config.contentType());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        baseRequest.setHandled(true);
        try {
            String replyText = ObjectCodeFactory.getObjectCodec(config.codec())
                    .encode(new ErrorResponse(e));
            response.getWriter().println(replyText);
        } catch (IOException ex) {
            log.error("Failed to send error " + e + ", " + this, ex);
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

}
