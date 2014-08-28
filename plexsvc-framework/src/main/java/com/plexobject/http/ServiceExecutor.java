package com.plexobject.http;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseDispatcher;
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
public class ServiceExecutor implements WebRequestHandler {
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
    public void handle(Method method, String uri, String payload,
            Map<String, Object> params, Map<String, Object> headers,
            AbstractResponseDispatcher dispatcher) {
        RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(method);
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(uri, params) : null;
        if (handler == null) {
            log.error("Unknown request received " + payload + "/" + params);
            return;
        }
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);
        dispatcher.setCodecType(config.codec());
        // dispatcher.setContentType(config.codec()
        // .getContentType());

        String sessionId = (String) headers.get(Constants.SESSION_ID);

        new RequestBuilder(handler, roleAuthorizer).setPayload(payload)
                .setParameters(params).setSessionId(sessionId)
                .setResponseDispatcher(dispatcher).invoke();
    }

}
