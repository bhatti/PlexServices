package com.plexobject.service.netty;

import java.util.Map;

import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.http.AbstractHttpServiceGateway;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.Configuration;

/**
 * This class provides gateway to services using http protocol
 * 
 * @author shahzad bhatti
 *
 */
public class HttpServiceGateway extends AbstractHttpServiceGateway {
    public HttpServiceGateway(
            final Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        super(config, authorizer, requestHandlerPathsByMethod, newNettyServer(
                config, authorizer, requestHandlerPathsByMethod));
    }

    private static NettyHttpServer newNettyServer(
            Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        try {
            final HttpRequestHandler handler = new HttpRequestHandler(
                    authorizer, requestHandlerPathsByMethod);
            return new NettyHttpServer(config, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
