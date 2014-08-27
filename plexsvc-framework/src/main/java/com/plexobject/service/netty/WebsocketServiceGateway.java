package com.plexobject.service.netty;

import java.util.Map;

import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.http.AbstractHttpServiceGateway;
import com.plexobject.service.route.RouteResolver;
import com.plexobject.util.Configuration;

public class WebsocketServiceGateway extends AbstractHttpServiceGateway {
    public WebsocketServiceGateway(
            final Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod,
            final String wsPath, final boolean ssl) {
        super(config, authorizer, requestHandlerPathsByMethod, newNettyServer(
                config, authorizer, requestHandlerPathsByMethod, wsPath, ssl));
    }

    private static NettyHttpServer newNettyServer(
            Configuration config,
            final RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod,
            final String wsPath, final boolean ssl) {
        try {
            final WebsocketRequestHandler handler = new WebsocketRequestHandler(
                    authorizer, requestHandlerPathsByMethod,
                    config.getDefaultCodecType(), wsPath, ssl);
            return new NettyHttpServer(config, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
