package com.plexobject.service.jetty;

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
            Configuration config,
            RoleAuthorizer authorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod) {
        super(config, authorizer, requestHandlerPathsByMethod,
                new JettyHttpServer(config, new HttpRequestHandler(authorizer,
                        requestHandlerPathsByMethod)));
    }
}
