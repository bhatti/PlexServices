package com.plexobject.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.plexobject.domain.Constants;
import com.plexobject.domain.Preconditions;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
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
        private CodecType codecType;
        private Response response;
        private ResponseDispatcher responseDispatcher;

        public Builder setProtocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setMethod(Method method) {
            this.method = method;
            return this;
        }

        public Builder setCodecType(CodecType codecType) {
            this.codecType = codecType;
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

        public Builder setResponseDispatcher(ResponseDispatcher dispatcher) {
            this.responseDispatcher = dispatcher;
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

        public Builder setResponse(Response response) {
            this.response = response;
            return this;
        }

        public Request build() {
            return new Request(protocol, method, endpoint, properties, headers,
                    payload, codecType, response, responseDispatcher);
        }
    }

    private ResponseDispatcher responseDispatcher;
    private Response response;
    private Protocol protocol;
    private Method method;
    private String endpoint;
    private CodecType codecType;

    Request() {
    }

    Request(Protocol protocol, Method method, String endpoint,
            final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload,
            final CodecType codecType, final Response response,
            final ResponseDispatcher responseDispatcher) {
        super(properties, headers, payload);
        Preconditions.requireNotNull(protocol, "protocol is required");
        Preconditions.requireNotNull(method, "method is required");
        Preconditions.requireNotNull(codecType, "codecType is required");
        Preconditions.requireNotNull(response, "response is required");
        Preconditions.requireNotNull(responseDispatcher,
                "responseDispatcher is required");
        this.protocol = protocol;
        this.method = method;
        this.endpoint = endpoint;
        this.codecType = codecType;
        this.response = response;
        this.responseDispatcher = responseDispatcher;
        if (response.getCodecType() == null) {
            response.setCodecType(codecType);
        }
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

    public CodecType getCodecType() {
        return codecType;
    }

    @JsonIgnoreProperties
    public ObjectCodec getCodec() {
        return codecType != null ? ObjectCodecFactory.getInstance()
                .getObjectCodec(codecType) : null;
    }

    @JsonIgnore
    public Response getResponse() {
        return response;
    }

    @JsonIgnore
    public ResponseDispatcher getResponseDispatcher() {
        return responseDispatcher;
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
