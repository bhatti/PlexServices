package com.plexobject.http.jetty;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            String endpoint = rawRequest.getEndpoint();
            if (endpoint == null) {
                log.error("Unknown request without endpoint " + jsonMsg
                        + ", rawRequest " + rawRequest);
                return;
            }
            for (String name : rawRequest.getPropertyNames()) {
                params.put(name, rawRequest.getProperty(name));
            }
            final String textPayload = codec.encode(rawRequest.getPayload());
            AbstractResponseDispatcher dispatcher = new JettyWebsocketResponseDispatcher(
                    session);

            com.plexobject.handler.Request req = com.plexobject.handler.Request
                    .builder().setMethod(Method.MESSAGE).setEndpoint(endpoint)
                    .setProperties(params).setHeaders(headers)
                    .setPayload(textPayload)
                    .setResponseDispatcher(dispatcher).build();

            handler.handle(req);
        }
    }
}
