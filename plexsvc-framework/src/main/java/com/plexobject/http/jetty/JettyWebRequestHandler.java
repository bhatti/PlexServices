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

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.http.WebRequestHandler;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.IOUtils;

public class JettyWebRequestHandler extends AbstractHandler {
    protected final WebRequestHandler handler;

    public JettyWebRequestHandler(WebRequestHandler handler) {
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
        handler.handle(method, uri,
                IOUtils.toString(baseRequest.getInputStream()),
                getParams(baseRequest), getHeaders(baseRequest), dispatcher);
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
