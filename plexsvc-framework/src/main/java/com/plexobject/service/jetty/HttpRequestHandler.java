package com.plexobject.service.jetty;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.plexobject.domain.Constants;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.IOUtils;

class HttpRequestHandler extends AbstractHandler {
    private RoleAuthorizer roleAuthorizer;
    private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;

    public HttpRequestHandler(RoleAuthorizer roleAuthorizer,
            Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
        this.roleAuthorizer = roleAuthorizer;
        this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
    }

    @Override
    public void handle(final String target, final Request baseRequest,
            final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        Map<String, Object> params = HttpServer.getParams(baseRequest);

        PathsLookup<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(Method.valueOf(baseRequest.getMethod()));
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(baseRequest.getPathInfo(), params) : null;
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);

        ResponseBuilder responseBuilder = new HttpResponseBuilder(
                config.contentType(), config.codec(), baseRequest, response);
        new RequestBuilder(handler, roleAuthorizer)
                .setPayload(IOUtils.toString(baseRequest.getInputStream()))
                .setParameters(params).setSessionId(getSessionId(baseRequest))
                .setRemoteAddress(baseRequest.getRemoteHost())
                .setResponseBuilder(responseBuilder).invoke();
    }

    private String getSessionId(Request baseRequest) {
        Cookie[] cookies = baseRequest.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(Constants.SESSION_ID)) {
                    return c.getValue();
                }
            }
        }

        return baseRequest.getHeader(Constants.SESSION_ID);
    }
}
