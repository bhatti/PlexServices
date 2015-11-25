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
        private HttpServletResponse httpResponse;

        public ServletBuilder setHttpRequest(HttpServletRequest httpRequest) {
            this.httpRequest = httpRequest;
            return this;
        }

        public ServletBuilder setHttpResponse(HttpServletResponse httpResponse) {
            this.httpResponse = httpResponse;
            return this;
        }

        public Request build() {
            if (remoteAddress != null) {
                properties.put(Constants.REMOTE_ADDRESS, remoteAddress);
            }
            return new ServletRequest(protocol, method, requestUri, endpoint,
                    replyEndpoint, properties, headers, contents, codecType,
                    responseDispatcher, httpRequest, httpResponse);
        }

    }

    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;

    public ServletRequest() {
        super();
    }

    public ServletRequest(Protocol protocol, RequestMethod method,
            String requestUri, String endpoint, String replyEndpoint,
            Map<String, Object> properties, Map<String, Object> headers,
            Object payload, CodecType codecType,
            ResponseDispatcher responseDispatcher,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        super(protocol, method, requestUri, endpoint, replyEndpoint,
                properties, headers, payload, codecType, responseDispatcher);
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    public static ServletBuilder builder() {
        return new ServletBuilder();
    }

}
