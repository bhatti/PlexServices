package com.plexobject.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.route.RouteResolver;

/**
 * This class looks up http handler by route and executes its handler
 * 
 * @author shahzad bhatti
 *
 */
public class DefaultHttpRequestHandler implements RequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(DefaultHttpRequestHandler.class);
    private final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod;
    private final ServiceRegistry serviceRegistry;

    public DefaultHttpRequestHandler(
            final ServiceRegistry serviceRegistry,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        this.serviceRegistry = serviceRegistry;
        this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
    }

    @Override
    public void handle(Request request) {
        RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(request.getMethod());
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(request.getEndpoint(), request.getProperties()) : null;
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

        serviceRegistry.invoke(request, handler);
    }
}