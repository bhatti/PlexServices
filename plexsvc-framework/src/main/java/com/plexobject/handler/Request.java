package com.plexobject.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.domain.Constants;
import com.plexobject.domain.Preconditions;
import com.plexobject.encode.CodecType;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

/**
 * This class encapsulates a request object
 * 
 * @author shahzad bhatti
 *
 */
public class Request extends BasePayload<Object> {
    protected static ThreadLocal<Request> currentRequest = new ThreadLocal<Request>();

    public static class Builder {
        protected Protocol protocol;
        protected Object contents;
        protected Map<String, Object> properties = new HashMap<>();
        protected Map<String, Object> headers = new HashMap<>();
        protected RequestMethod method;
        protected String remoteAddress;
        protected String requestUri;
        protected String endpoint;
        protected String replyEndpoint;
        protected CodecType codecType;
        protected ResponseDispatcher responseDispatcher;

        public Builder setProtocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setMethod(RequestMethod method) {
            this.method = method;
            return this;
        }

        public Builder setCodecType(CodecType codecType) {
            this.codecType = codecType;
            return this;
        }

        public Builder setRequestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }

        public Builder setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder setReplyEndpoint(String replyEndpoint) {
            this.replyEndpoint = replyEndpoint;
            return this;
        }

        public Builder setContents(Object payload) {
            this.contents = payload;
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

        public Builder setRemoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Request build() {
            if (remoteAddress != null) {
                properties.put(Constants.REMOTE_ADDRESS, remoteAddress);
            }
            return new Request(protocol, method, requestUri, endpoint,
                    replyEndpoint, properties, headers, contents, codecType,
                    responseDispatcher);
        }

    }

    protected transient ResponseDispatcher responseDispatcher;
    protected transient Response response;
    protected Protocol protocol;
    protected RequestMethod method;
    protected String endpoint;
    protected String replyEndpoint;
    protected String methodName;
    protected String requestUri;
    protected Object lastSentContents;

    protected Request() {
    }

    protected Request(final Protocol protocol, final RequestMethod method,
            final String requestUri, final String endpoint,
            final String replyEndpoint, final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload,
            final CodecType codecType,
            final ResponseDispatcher responseDispatcher) {
        super(codecType, properties, headers, payload);
        Preconditions.requireNotNull(protocol, "protocol is required");
        Preconditions.requireNotNull(method, "method is required");
        Preconditions.requireNotNull(responseDispatcher,
                "responseDispatcher is required");
        this.protocol = protocol;
        this.method = method;
        this.requestUri = requestUri == null ? endpoint : requestUri;
        this.endpoint = endpoint;
        this.replyEndpoint = replyEndpoint;
        this.responseDispatcher = responseDispatcher;
        this.response = new Response(this, new HashMap<String, Object>(),
                new HashMap<String, Object>(), null, codecType);
        currentRequest.set(this);
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getReplyEndpoint() {
        return replyEndpoint;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getRemoteAddress() {
        return (String) properties.get(Constants.REMOTE_ADDRESS);
    }

    public String getSessionId() {
        String sessionId = (String) this.properties.get(Constants.SESSION_ID);
        if (sessionId == null) {
            sessionId = (String) this.headers.get(Constants.SESSION_ID);
        }

        return sessionId;
    }



    @JsonIgnore
    public Response getResponse() {
        return response;
    }

    @JsonIgnore
    public ResponseDispatcher getResponseDispatcher() {
        return responseDispatcher;
    }

    public synchronized void sendResponse() {
        if (response.getContents() != null) {
            if (response.getContents() == lastSentContents) {
                throw new IllegalStateException("Response already sent "
                        + response.getContents());
            }
            lastSentContents = response.getContents();
            responseDispatcher.send(response);
        }
    }

    @SuppressWarnings("unchecked")
    @JsonAnySetter
    public void handleUnknown(String key, Object value) {
        if (value instanceof String || value instanceof Number
                || value instanceof Boolean || value instanceof Character) {
            if (Constants.CONTENTS.equals(key)) {
                contents = value;
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
        return "Request [method=" + method + ", requestUri=" + requestUri
                + ", endpoint=" + endpoint + ", properties=" + properties
                + ", headers=" + headers + ", createdAt=" + createdAt
                + ", contents=" + contents + "]";
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContentsAs() {
        return (T) contents;
    }

    public static Request getCurrentRequest() {
        return currentRequest.get();
    }
}
