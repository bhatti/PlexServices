package com.plexobject.service.jetty;

import java.util.Map;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.util.Configuration;

public class WebsocketServiceGateway extends AbstractHttpServiceGateway {
    public static class WebsocketConfigCreator implements WebSocketCreator {
        private final RoleAuthorizer authorizer;
        private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;

        private WebsocketConfigCreator(
                RoleAuthorizer authorizer,
                final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
            this.authorizer = authorizer;
            this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
        }

        @Override
        public Object createWebSocket(ServletUpgradeRequest req,
                ServletUpgradeResponse resp) {
            for (String subprotocol : req.getSubProtocols()) {
                if ("text".equals(subprotocol)) { // "binary"
                    resp.setAcceptedSubProtocol(subprotocol);
                    return new WebsocketRequestHandler(authorizer,
                            requestHandlerPathsByMethod);
                }
            }
            return null;
        }
    }

    public static class WebsocketConfigHandler extends WebSocketHandler {
        private final RoleAuthorizer authorizer;
        private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;

        private WebsocketConfigHandler(
                RoleAuthorizer authorizer,
                final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
            this.authorizer = authorizer;
            this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
        }

        @Override
        public void configure(WebSocketServletFactory factory) {
            factory.setCreator(new WebsocketConfigCreator(authorizer,
                    requestHandlerPathsByMethod));
            // factory.register(WebsocketRequestHandler.class);
        }
    }

    public WebsocketServiceGateway(
            Configuration config,
            RoleAuthorizer authorizer,
            final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
        super(config, authorizer, new WebsocketConfigHandler(authorizer,
                requestHandlerPathsByMethod), requestHandlerPathsByMethod);
    }
}