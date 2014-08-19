package com.plexobject.service.jetty;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.handler.Response;

public class WebsocketResponseBuilder extends AbstractResponseBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(WebsocketResponseBuilder.class);
    private final Session session;
    private final Map<String, Object> properties = new HashMap<>();

    public WebsocketResponseBuilder(final CodecType codecType,
            final Session session) {
        super(codecType);
        this.session = session;
    }

    public void addSessionId(String value) {
        properties.put(Constants.SESSION_ID, value);
    }

    @Override
    public void send(Object payload) {
        String sessionId = (String) properties.get(Constants.SESSION_ID);
        if (sessionId != null) {
            addSessionId(sessionId);
        }
        try {
            Response response = new Response(properties, payload);
            String responseText = ObjectCodecFactory.getInstance()
                    .getObjectCodec(codecType).encode(response);
            session.getRemote().sendString(responseText);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to send " + payload + ", " + this, e);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        String sessionId = (String) properties.get(Constants.SESSION_ID);
        if (sessionId != null) {
            sessionId.hashCode();
        }
        String host = session.getRemoteAddress().getHostName();
        return host.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WebsocketResponseBuilder other = (WebsocketResponseBuilder) obj;

        String sessionId = (String) properties.get(Constants.SESSION_ID);
        String otherSessionId = (String) other.properties
                .get(Constants.SESSION_ID);
        if (sessionId != null && otherSessionId != null) {
            return sessionId.equals(otherSessionId);
        }
        String host = session.getRemoteAddress().getHostName();
        String otherHost = other.session.getRemoteAddress().getHostName();
        if (host != null && otherHost != null) {
            return host.equals(otherHost);
        }
        return false;
    }

    @Override
    public String toString() {
        return "WebsocketResponse " + session.getRemoteAddress().getHostName();
    }

}
