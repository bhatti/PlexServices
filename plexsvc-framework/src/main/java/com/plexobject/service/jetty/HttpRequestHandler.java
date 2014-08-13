package com.plexobject.service.jetty;

import java.io.IOException;
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
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.IOUtils;

/**
 * This class extends jetty's AbstractHandler, which is invoked when web request
 * is received
 * 
 * @author shahzad bhatti
 *
 */
class HttpRequestHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory
            .getLogger(HttpRequestHandler.class);
    private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;

    private RoleAuthorizer roleAuthorizer;

    public HttpRequestHandler(
            RoleAuthorizer roleAuthorizer,
            final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
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
        if (handler == null) {
            log.error("Unknown request received "
                    + HttpResponseBuilder.toString(request));
            return;
        }
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);

        AbstractResponseBuilder responseBuilder = new HttpResponseBuilder(
                config.codec(), baseRequest, response);
        new RequestBuilder(handler, roleAuthorizer)
                .setPayload(IOUtils.toString(baseRequest.getInputStream()))
                .setParameters(params).setSessionId(getSessionId(baseRequest))
                .setRemoteAddress(baseRequest.getRemoteHost())
                .setResponseBuilder(responseBuilder).invoke();
    }

    private static String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(Constants.SESSION_ID)) {
                    return c.getValue();
                }
            }
        }

        return request.getHeader(Constants.SESSION_ID);
    }
}
