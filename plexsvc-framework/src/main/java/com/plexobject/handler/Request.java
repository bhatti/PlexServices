package com.plexobject.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.domain.Constants;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;

/**
 * This class encapsulates a request object
 * 
 * @author shahzad bhatti
 *
 */
public class Request extends AbstractPayload {
    public static class Builder {
        private Protocol protocol;
        private Object payload;
        private Map<String, Object> properties = new HashMap<>();
        private Map<String, Object> headers = new HashMap<>();
        private Method method;
        private String endpoint;

        private AbstractResponseDispatcher responseBuilder;

        public Builder setProtocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setMethod(Method method) {
            this.method = method;
            return this;
        }

        public Builder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder setPayload(Object payload) {
            this.payload = payload;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            if (sessionId != null) {
                this.headers.put(Constants.SESSION_ID, sessionId);
            }
            return this;
        }

        public Builder setProperties(Map<String, Object> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public Builder setHeaders(Map<String, Object> headers) {
            this.headers.putAll(headers);
            return this;
        }

        public Builder setResponseDispatcher(
                AbstractResponseDispatcher responseBuilder) {
            this.responseBuilder = responseBuilder;
            return this;
        }

        public Request build() {
            return new Request(protocol, method, endpoint, properties, headers,
                    payload, responseBuilder);
        }
    }

    private AbstractResponseDispatcher responseBuilder;
    private Protocol protocol;
    private Method method;
    private String endpoint;

    Request() {
    }

    public Request(Protocol protocol, Method method, String endpoint,
            final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload,
            final AbstractResponseDispatcher responseBuilder) {
        super(properties, headers, payload);
        this.protocol = protocol;
        this.method = method;
        this.endpoint = endpoint;
        this.responseBuilder = responseBuilder;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Method getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getSessionId() {
        String sessionId = (String) this.properties.get(Constants.SESSION_ID);
        if (sessionId == null) {
            sessionId = (String) this.headers.get(Constants.SESSION_ID);
        }

        return sessionId;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public <T extends AbstractResponseDispatcher> T getResponseDispatcher() {
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
        return "Request [method=" + method + ", endpoint=" + endpoint
                + ", properties=" + properties + ", headers=" + headers
                + ", createdAt=" + createdAt + ", payload=" + payload + "]";
    }

    public static Builder builder() {
        return new Builder();
    }
}
