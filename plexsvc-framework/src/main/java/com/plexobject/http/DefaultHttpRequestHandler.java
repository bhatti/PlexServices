package com.plexobject.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.route.RouteResolver;
import com.plexobject.service.Method;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

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
            request.getResponseDispatcher().setCodecType(CodecType.TEXT);
            request.getResponseDispatcher()
                    .setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponseDispatcher().send(
                    "Unknown request received payload " + request.getPayload()
                            + " for endpoint " + request.getEndpoint()
                            + ",  method " + request.getMethod()
                            + ", protocol " + request.getProtocol()
                            + ", properties " + request.getProperties());

            log.error("** Unknown request received payload "
                    + request.getPayload()
                    + " for endpoint "
                    + request.getEndpoint()
                    + ",  method "
                    + request.getMethod()
                    + ", protocol "
                    + request.getProtocol()
                    + ", properties "
                    + request.getProperties()
                    + ", available requestHandlerPaths "
                    + (requestHandlerPaths != null ? requestHandlerPaths
                            : requestHandlerPathsByMethod));
            return;
        }
        ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
        request.getResponseDispatcher().setCodecType(
                CodecType.fromAcceptHeader(
                        (String) request.getHeader(Constants.ACCEPT),
                        config.codec()));
        // dispatcher.setContentType(config.codec()
        // .getContentType());

        serviceRegistry.invoke(request, handler);
    }
}
