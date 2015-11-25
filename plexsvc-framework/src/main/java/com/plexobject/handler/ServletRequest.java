package com.plexobject.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class ServletRequest extends Request {
    public static class ServletBuilder extends Builder {
        private HttpServletRequest httpRequest;
        private HttpServletResponse httpResonse;

        public ServletBuilder setHttpRequest(HttpServletRequest httpRequest) {
            this.httpRequest = httpRequest;
            return this;
        }

        public ServletBuilder setHttpResonse(HttpServletResponse httpResonse) {
            this.httpResonse = httpResonse;
            return this;
        }

        public Request build() {
            if (remoteAddress != null) {
                properties.put(Constants.REMOTE_ADDRESS, remoteAddress);
            }
            return new ServletRequest(protocol, method, requestUri, endpoint,
                    replyEndpoint, properties, headers, contents, codecType,
                    responseDispatcher, httpRequest, httpResonse);
        }

    }

    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResonse;

    public ServletRequest() {
        super();
    }

    public ServletRequest(Protocol protocol, RequestMethod method,
            String requestUri, String endpoint, String replyEndpoint,
            Map<String, Object> properties, Map<String, Object> headers,
            Object payload, CodecType codecType,
            ResponseDispatcher responseDispatcher,
            HttpServletRequest httpRequest, HttpServletResponse httpResonse) {
        super(protocol, method, requestUri, endpoint, replyEndpoint,
                properties, headers, payload, codecType, responseDispatcher);
        this.httpRequest = httpRequest;
        this.httpResonse = httpResonse;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpServletResponse getHttpResonse() {
        return httpResonse;
    }

    public static Builder builder() {
        return new ServletBuilder();
    }

}
