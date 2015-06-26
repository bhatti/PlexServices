package com.plexobject.handler;

import java.util.Map;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.http.HttpResponse;

/**
 * This class encapsulates response
 * 
 * @author shahzad bhatti
 *
 */
public class Response extends AbstractPayload {
    public static final String[] HEADER_PROPERTIES = new String[] {
            HttpResponse.STATUS, HttpResponse.LOCATION, Constants.SESSION_ID };
    private CodecType codecType;
    private int status = HttpResponse.SC_OK;
    private final Request request;

    public Response(final Request request,
            final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload,
            CodecType codecType) {
        super(properties, headers, payload);
        this.request = request;
        this.codecType = codecType;
    }

    public Request getRequest() {
        return request;
    }
    public Response setStatus(int status) {
        this.status = status;
        return this;
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

    public int getStatus() {
        if (status > 0) {
            return status;
        } else {
            Object status = properties.get(HttpResponse.STATUS);
            if (status != null) {
                if (status instanceof Number) {
                    return ((Number) status).intValue();
                } else if (status instanceof String) {
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

    public CodecType getCodecType() {
        return codecType;
    }

    @Override
    public String toString() {
        return "Response getPayload()=" + getPayload() + "]";
    }

}
