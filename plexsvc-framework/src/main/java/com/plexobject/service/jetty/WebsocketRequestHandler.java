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
import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestBuilder;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;

/**
 * This class implements websocket handler for incoming web requests
 * 
 * @author shahzad bhatti
 *
 */
@WebSocket
public class WebsocketRequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(WebsocketRequestHandler.class);

    private RoleAuthorizer roleAuthorizer;
    private final Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod;

    public WebsocketRequestHandler(RoleAuthorizer roleAuthorizer,
            Map<Method, PathsLookup<RequestHandler>> requestHandlerPathsByMethod) {
        this.roleAuthorizer = roleAuthorizer;
        this.requestHandlerPathsByMethod = requestHandlerPathsByMethod;
    }

    @OnWebSocketMessage
    public void onWebSocketText(Session session, String jsonMsg) {
        if (session.isOpen()) {
        }
        Map<String, Object> params = new HashMap<>();
        Request rawRequest = ObjectCodeFactory.getObjectCodec(CodecType.JSON)
                .decode(jsonMsg, Request.class, params);
        String endpoint = rawRequest.getStringProperty(Constants.ENDPOINT);
        if (endpoint == null) {
            log.error("Unknown request without endpoint " + jsonMsg);
            return;
        }
        //
        PathsLookup<RequestHandler> requestHandlerPaths = requestHandlerPathsByMethod
                .get(Method.MESSAGE);
        RequestHandler handler = requestHandlerPaths != null ? requestHandlerPaths
                .get(endpoint, params) : null;
        if (handler == null) {
            log.error("Unknown request received " + jsonMsg);
            return;
        }
        String payload = rawRequest.getPayload();
        ServiceConfig config = handler.getClass().getAnnotation(
                ServiceConfig.class);

        AbstractResponseBuilder responseBuilder = new WebsocketResponseBuilder(
                config.contentType(), config.codec(), session);
        new RequestBuilder(handler, roleAuthorizer).setPayload(payload)
                .setParameters(params).setSessionId(rawRequest.getSessionId())
                .setRemoteAddress(session.getRemoteAddress().getHostName())
                .setResponseBuilder(responseBuilder).invoke();
    }
}
