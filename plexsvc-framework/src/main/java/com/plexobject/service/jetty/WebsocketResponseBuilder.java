package com.plexobject.service.jetty;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.handler.Response;

public class WebsocketResponseBuilder extends AbstractResponseBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(WebsocketResponseBuilder.class);
    private final Session session;
    private final Map<String, Object> properties = new HashMap<>();

    public WebsocketResponseBuilder(final String contentType,
            final CodecType codecType, final Session session) {
        super(contentType, codecType);
        this.session = session;
    }

    public void addSessionId(String value) {
        properties.put(Constants.SESSION_ID, value);
    }

    protected void doSend(String payload) {
        try {
            Response response = new Response(properties, payload);
            String responseText = ObjectCodeFactory.getObjectCodec(
                    CodecType.JSON).encode(response);
            session.getRemote().sendString(responseText);
        } catch (Exception e) {
            log.error("Failed to send " + payload + ", " + this, e);
        }
    }

    @Override
    public String toString() {
        return "WebsocketResponse " + session.getRemoteAddress().getHostName();
    }

}
