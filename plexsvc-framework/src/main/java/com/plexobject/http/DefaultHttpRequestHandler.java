package com.plexobject.http;

import java.util.Map;

import org.apache.log4j.Logger;

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
    public void handle(Request request) {
        RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(request.getMethod());
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(request.getEndpoint(), request.getProperties()) : null;
        if (handler == null) {
            request.getResponse().setCodecType(CodecType.TEXT);
            request.getResponse().setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponse().setPayload(
                    "Unknown request received payload " + request);

            log.error("** Unknown request '"
                    + request.getEndpoint()
                    + ", received payload "
                    + request
                    + ", available requestHandlerPaths "
                    + (requestHandlerPaths != null ? requestHandlerPaths
                            : requestHandlerPathsByMethod));
            return;
        }
        ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
        // TODO probably unnecessary to set codec type
        request.getResponse().setCodecType(
                CodecType.fromAcceptHeader(
                        (String) request.getHeader(Constants.ACCEPT),
                        config.codec()));
        // dispatcher.setContentType(config.codec()
        // .getContentType());

        serviceRegistry.invoke(request, handler);
    }
}
