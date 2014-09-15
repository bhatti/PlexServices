package com.plexobject.http.jetty;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.IOUtils;

public class JettyWebRequestHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory
            .getLogger(JettyWebRequestHandler.class);

    protected final RequestHandler handler;

    public JettyWebRequestHandler(RequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handle(final String target, final Request baseRequest,
            final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        AbstractResponseDispatcher dispatcher = new JettyResponseDispatcher(
                baseRequest, response);
        Method method = Method.valueOf(baseRequest.getMethod());
        String uri = baseRequest.getPathInfo();
        Map<String, Object> headers = getHeaders(baseRequest);

        Map<String, Object> params = getParams(baseRequest);
        String payload = IOUtils.toString(baseRequest.getInputStream());

        com.plexobject.handler.Request req = com.plexobject.handler.Request
                .builder().setMethod(method).setEndpoint(uri)
                .setProperties(params).setHeaders(headers).setPayload(payload)
                .setResponseDispatcher(dispatcher).build();
        log.info("Received " + req);

        handler.handle(req);
    }

    public static Map<String, Object> getParams(HttpServletRequest request) {
        Map<String, Object> params = new HashMap<>();

        for (Map.Entry<String, String[]> e : request.getParameterMap()
                .entrySet()) {
            params.put(e.getKey(), e.getValue()[0]);
        }
        return params;
    }

    public static Map<String, Object> getHeaders(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                result.put(c.getName(), c.getValue());
            }
        }
        result.put(Constants.HOST, request.getRemoteHost());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            result.put(name, request.getHeaders(name).nextElement());
        }
        return result;
    }

}
