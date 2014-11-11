package com.plexobject.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.http.HttpResponse;
import com.plexobject.util.ExceptionUtils;

public abstract class AbstractResponseDispatcher implements ResponseDispatcher {
    private static final Logger log = LoggerFactory
            .getLogger(AbstractResponseDispatcher.class);

    protected static final String[] HEADER_PROPERTIES = new String[] {
            HttpResponse.STATUS, HttpResponse.LOCATION, Constants.SESSION_ID };
    protected CodecType codecType;
    private int status = HttpResponse.SC_OK;
    protected final Map<String, Object> properties = new HashMap<>();

    public AbstractResponseDispatcher setStatus(int status) {
        this.status = status;
        return this;
    }

    public AbstractResponseDispatcher setCodecType(CodecType type) {
        this.codecType = type;
        return this;
    }

    public AbstractResponseDispatcher setProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    public void send(Object payload) {
        String sessionId = (String) properties.get(Constants.SESSION_ID);
        if (sessionId != null) {
            addSessionId(sessionId);
        }
        String replyText = null;
        if (payload instanceof Exception) {
            log.warn("Error received " + payload, payload);
            Map<String, Object> attributes = ExceptionUtils
                    .toMap((Exception) payload);
            replyText = ObjectCodecFactory.getInstance()
                    .getObjectCodec(codecType).encode(attributes);
            for (String name : HEADER_PROPERTIES) {
                Object value = attributes.get(name);
                if (value != null) {
                    properties.put(name, value);
                }
            }
        } else {
            replyText = payload instanceof String ? (String) payload
                    : ObjectCodecFactory.getInstance()
                            .getObjectCodec(codecType).encode(payload);
        }
        doSend(replyText);
    }

    protected void doSend(String payload) {

    }

    public abstract void addSessionId(String value);

    public int getStatus() {
        if (status > 0) {
            return status;
        } else {
            Object status = properties.get(HttpResponse.STATUS);
            if (status != null) {
                if (status instanceof Integer) {
                    return (Integer) status;
                } else if (status instanceof Integer) {
                    return Integer.parseInt((String) status);
                }
            }
        }
        return status;
    }

    @Override
    public int hashCode() {
        String sessionId = (String) properties.get(Constants.SESSION_ID);
        if (sessionId != null) {
            sessionId.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractResponseDispatcher other = (AbstractResponseDispatcher) obj;

        String sessionId = (String) properties.get(Constants.SESSION_ID);
        String otherSessionId = (String) other.properties
                .get(Constants.SESSION_ID);
        if (sessionId != null && otherSessionId != null) {
            return sessionId.equals(otherSessionId);
        }
        return false;
    }
}
