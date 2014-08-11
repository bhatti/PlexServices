package com.plexobject.handler;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.domain.Constants;

/**
 * This class encapsulates remote request
 * 
 * @author shahzad bhatti
 *
 */
public class Request extends AbstractPayload {
    private String sessionId;
    private String remoteAddress;
    private ResponseDispatcher responseBuilder;

    Request() {

    }

    public Request(final Map<String, Object> properties, final Object payload,
            final String sessionId, final String remoteAddress,
            final ResponseDispatcher responseBuilder) {
        super(properties, payload);
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.responseBuilder = responseBuilder;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T extends AbstractResponseBuilder> T getResponseBuilder() {
        return (T) responseBuilder;
    }

    @SuppressWarnings("unchecked")
    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        if (value instanceof String || value instanceof Number
                || value instanceof Boolean || value instanceof Character) {
            if (Constants.SESSION_ID.equals(key)) {
                sessionId = (String) value;
                setProperty(key, value);
            } else if (Constants.PAYLOAD.equals(key)) {
                payload = value;
            } else {
                setProperty(key, value);
            }
        } else if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                handleUnknown(e.getKey(), e.getValue());
            }
        }
    }

    @Override
    public String toString() {
        return "Request [sessionId=" + sessionId + ", remoteAddress="
                + remoteAddress + ", properties=" + properties + ", createdAt="
                + createdAt + ", payload=" + payload + "]";
    }

}
