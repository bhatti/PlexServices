package com.plexobject.http;

import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.route.RouteResolver;
import com.plexobject.service.Method;
import com.plexobject.service.ServiceRegistry;

/**
 * This class looks up http handler by route and executes its handler
 * 
 * @author shahzad bhatti
 *
 */
public class DefaultHttpRequestHandler implements RequestHandler {
    private static final Logger log = Logger
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
    public void handle(Request<Object> request) {
        RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(request.getMethod());
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(request.getEndpoint(), request.getProperties()) : null;
        if (handler == null) {
            request.getResponse().setCodecType(CodecType.TEXT);
            request.getResponse().setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponse().setPayload(
                    "Unknown request received payload " + request);
            request.getResponseDispatcher().send(request.getResponse());
            log.error("** Unknown request '"
                    + request.getEndpoint()
                    + ", received payload "
                    + request
                    + ", available requestHandlerPaths "
                    + (requestHandlerPaths != null ? requestHandlerPaths
                            : requestHandlerPathsByMethod));
            return;
        }
        serviceRegistry.invoke(request, handler);
    }
}
