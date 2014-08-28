package com.plexobject.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.route.RouteResolver;

/**
 * This class executes http service
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceExecutor implements RequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(ServiceExecutor.class);
    private final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod;

    private RoleAuthorizer roleAuthorizer;

    public ServiceExecutor(
            RoleAuthorizer roleAuthorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        this.roleAuthorizer = roleAuthorizer;
        this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
    }

    @Override
    public void handle(Request request) {
        RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(request.getMethod());
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(request.getUri(), request.getProperties()) : null;
        if (handler == null) {
            log.error("Unknown request received " + request.getPayload() + "/"
                    + request.getProperties());
            return;
        }
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);
        request.getResponseDispatcher().setCodecType(config.codec());
        // dispatcher.setContentType(config.codec()
        // .getContentType());

        String sessionId = (String) request.getHeader(Constants.SESSION_ID);

        new RequestBuilder(handler, roleAuthorizer)
                .setPayload(request.getPayload())
                .setParameters(request.getProperties()).setSessionId(sessionId)
                .setResponseDispatcher(request.getResponseDispatcher())
                .invoke();
    }

}
