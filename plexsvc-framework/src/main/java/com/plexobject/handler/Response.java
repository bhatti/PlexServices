package com.plexobject.handler;

import java.util.Map;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.http.HttpResponse;
import com.plexobject.util.HostUtils;

/**
 * This class encapsulates response
 * 
 * @author shahzad bhatti
 *
 */
public class Response extends BasePayload<Object> {
    public static final String[] HEADER_PROPERTIES = new String[] {
            HttpResponse.STATUS_CODE, HttpResponse.STATUS_MESSAGE,
            HttpResponse.LOCATION, Constants.SESSION_ID,
            HttpResponse.VERSION_HEADER };
    private final Request request;

    public Response(final Request request,
            final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload,
            CodecType codecType) {
        super(request.getRequestId(), codecType, properties, headers, payload);
        headers.put(Constants.REMOTE_ADDRESS, HostUtils.getLocalHost());
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public Response setCodecType(CodecType type) {
        this.codecType = type;
        return this;
    }

    public Response setLocation(String location) {
        properties.put(HttpResponse.LOCATION, location);
        return this;
    }

    public Response addSessionId(String value) {
        properties.put(Constants.SESSION_ID, value);
        return this;
    }

    public Response setVersionHeader(String version) {
        properties.put(HttpResponse.VERSION_HEADER, version);
        return this;
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
        if (obj.getClass() != getClass()) {
            return false;
        }
        Response other = (Response) obj;

        String sessionId = (String) properties.get(Constants.SESSION_ID);
        String otherSessionId = (String) other.properties
                .get(Constants.SESSION_ID);
        if (sessionId != null && otherSessionId != null) {
            return sessionId.equals(otherSessionId);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public <T> T getContentsAs() {
        return (T) contents;
    }

    @Override
    public String toString() {
        return "Response getContents()=" + getContents() + "]";
    }

}
