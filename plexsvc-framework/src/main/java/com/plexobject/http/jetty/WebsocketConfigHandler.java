package com.plexobject.http.jetty;

import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import com.plexobject.domain.ObjectFactory;

public class WebsocketConfigHandler extends WebSocketHandler {
    public static class WebsocketConfigCreator implements WebSocketCreator {
        private final ObjectFactory<Object> websocketFactory;

        private WebsocketConfigCreator(
                final ObjectFactory<Object> websocketFactory) {
            this.websocketFactory = websocketFactory;
        }

        @Override
        public Object createWebSocket(ServletUpgradeRequest req,
                ServletUpgradeResponse resp) {
            return websocketFactory.create(req, resp);
        }
    }

    private final ObjectFactory<Object> websocketFactory;

    public WebsocketConfigHandler(final ObjectFactory<Object> websocketFactory) {
        this.websocketFactory = websocketFactory;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(new WebsocketConfigCreator(websocketFactory));
        // factory.register(WebsocketRequestHandler.class);
    }
}
