package com.plexobject.service.jetty;

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
import com.plexobject.handler.AbstractResponseDelegate;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.route.RouteResolver;

/**
 * This class implements websocket handler for incoming web requests
 * 
 * @author shahzad bhatti
 *
 */
@WebSocket
class WebsocketRequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(WebsocketRequestHandler.class);

    private RoleAuthorizer roleAuthorizer;
    private final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod;
    private final ObjectCodec codec;

    public WebsocketRequestHandler(
            final RoleAuthorizer roleAuthorizer,
            final Map<Method, RouteResolver<RequestHandler>> requestHandlerPathsByMethod,
            final CodecType codecType) {
        this.roleAuthorizer = roleAuthorizer;
        this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
        this.codec = ObjectCodecFactory.getInstance().getObjectCodec(codecType);
    }

    @OnWebSocketMessage
    public void onWebSocketText(Session session, String jsonMsg) {
        if (session.isOpen()) {
        }
        Map<String, Object> params = new HashMap<>();
        Request rawRequest = codec.decode(jsonMsg, Request.class, params);
        String endpoint = rawRequest.getStringProperty(Constants.ENDPOINT);
        if (endpoint == null) {
            log.error("Unknown request without endpoint " + jsonMsg);
            return;
        }
        //
        RouteResolver<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(Method.MESSAGE);
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(endpoint, params) : null;
        if (handler == null) {
            log.error("Unknown request received " + jsonMsg);
            return;
        }
        for (String name : rawRequest.getPropertyNames()) {
            params.put(name, rawRequest.getProperty(name));
        }
        final String textPayload = codec.encode(rawRequest.getPayload());

        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);

        AbstractResponseDelegate responseBuilder = new WebsocketResponseDelegate(
                config.codec(), session);
        new RequestBuilder(handler, roleAuthorizer).setPayload(textPayload)
                .setParameters(params).setSessionId(rawRequest.getSessionId())
                .setRemoteAddress(session.getRemoteAddress().getHostName())
                .setResponseDispatcher(responseBuilder).invoke();
    }
}
