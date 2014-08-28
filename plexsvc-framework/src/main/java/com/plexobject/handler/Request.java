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
    private ResponseDispatcher responseBuilder;

    Request() {

    }

    public Request(final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload,
            final String sessionId, final ResponseDispatcher responseBuilder) {
        super(properties, headers, payload);
        this.sessionId = sessionId;
        this.responseBuilder = responseBuilder;
    }

    public String getSessionId() {
        return sessionId;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T extends AbstractResponseDispatcher> T getResponseBuilder() {
        return (T) responseBuilder;
    }

    @SuppressWarnings("unchecked")
    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        if (value instanceof String || value instanceof Number
                || value instanceof Boolean || value instanceof Character) {
            if (Constants.PAYLOAD.equals(key)) {
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
        return "Request [sessionId=" + sessionId + ", properties=" + properties
                + ", createdAt=" + createdAt + ", payload=" + payload + "]";
    }

}
