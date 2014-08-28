package com.plexobject.http.jetty;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig.Method;

@WebSocket
public class JettyWebsocketRequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(JettyWebsocketRequestHandler.class);
    private final ObjectCodec codec;

    private final RequestHandler handler;

    public JettyWebsocketRequestHandler(RequestHandler handler,
            final CodecType codecType) {
        this.handler = handler;
        this.codec = ObjectCodecFactory.getInstance().getObjectCodec(codecType);
    }

    @OnWebSocketMessage
    public void onWebSocketText(Session session, String jsonMsg) {
        if (session.isOpen()) {
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> headers = new HashMap<>();
            Request rawRequest = codec.decode(jsonMsg, Request.class, params);
            String endpoint = rawRequest.getStringProperty(Constants.ENDPOINT);
            if (endpoint == null) {
                log.error("Unknown request without endpoint " + jsonMsg);
                return;
            }
            for (String name : rawRequest.getPropertyNames()) {
                params.put(name, rawRequest.getProperty(name));
            }
            final String textPayload = codec.encode(rawRequest.getPayload());
            AbstractResponseDispatcher dispatcher = new JettyWebsocketResponseDispatcher(
                    session);

            String sessionId = (String) headers.get(Constants.SESSION_ID);
            com.plexobject.handler.Request req = new com.plexobject.handler.Request(
                    Method.MESSAGE, endpoint, params, headers, textPayload,
                    sessionId, dispatcher);
            handler.handle(req);
        }
    }
}