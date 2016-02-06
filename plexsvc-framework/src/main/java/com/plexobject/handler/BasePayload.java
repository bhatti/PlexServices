package com.plexobject.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.domain.Statusable;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.http.HttpResponse;

public abstract class BasePayload<T> implements Statusable {
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE = "Content-Type";
    protected long requestId;
    protected final Map<String, Object> properties;
    protected final Map<String, Object> headers;
    protected final long createdAt;
    protected CodecType codecType;
    protected T contents;
    protected int statusCode = 0;
    protected String statusMessage;

    BasePayload() {
        this.createdAt = System.currentTimeMillis();
        this.properties = new HashMap<>();
        this.headers = new HashMap<>();
    }

    protected BasePayload(long requestId, final CodecType codecType,
            final Map<String, Object> properties,
            final Map<String, Object> headers, final T contents) {
        this.requestId = requestId;
        this.createdAt = System.currentTimeMillis();
        this.codecType = codecType;
        this.properties = properties;
        this.headers = headers;
        this.contents = contents;
    }

    public CodecType getCodecType() {
        return codecType;
    }

    @JsonIgnore
    public ObjectCodec getCodec() {
        return codecType != null ? ObjectCodecFactory.getInstance()
                .getObjectCodec(codecType) : null;
    }

    public void setContents(T obj) {
        this.contents = obj;
        if (contents instanceof Statusable) {
            setStatusCode(((Statusable) contents).getStatusCode());
        }
    }

    public boolean isGoodStatus() {
        return getStatusCode() < HttpResponse.SC_MULTIPLE_CHOICES
                && getStatusMessage() == null;
    }

    @Override
    public int getStatusCode() {
        if (statusCode > 0) {
            return statusCode;
        } else {
            Object value = properties.get(HttpResponse.STATUS_CODE);
            if (value != null) {
                if (value instanceof Number) {
                    setStatusCode(((Number) value).intValue());
                    return statusCode;
                } else if (value instanceof String) {
                    setStatusCode(Integer.parseInt((String) value));
                    return statusCode;
                }
            }
        }
        return HttpResponse.SC_OK;
    }

    public BasePayload<T> setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        properties.put(HttpResponse.STATUS_CODE, statusCode);
        return this;
    }

    @Override
    public String getStatusMessage() {
        if (statusMessage != null) {
            return statusMessage;
        } else {
            String value = (String) properties.get(HttpResponse.STATUS_MESSAGE);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public BasePayload<T> setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        properties.put(HttpResponse.STATUS_MESSAGE, statusMessage);
        return this;
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    @JsonIgnore
    public Map<String, Object> getPropertiesAndHeaders() {
        Map<String, Object> propsAndHeaders = new HashMap<>(getHeaders());
        propsAndHeaders.putAll(getProperties());
        return propsAndHeaders;
    }

    @SuppressWarnings("unchecked")
    public <V> V getProperty(String name) {
        return (V) properties.get(name);
    }

    public Integer getIntegerProperty(String name) {
        Object value = properties.get(name);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else if (value != null) {
            return Integer.valueOf(value.toString());
        } else {
            return null;
        }
    }

    public Long getLongProperty(String name) {
        Object value = properties.get(name);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof String) {
            return Long.valueOf((String) value);
        } else if (value != null) {
            return Long.valueOf(value.toString());
        } else {
            return null;
        }
    }

    public boolean getBooleanProperty(String name, boolean def) {
        Boolean value = getBooleanProperty(name);
        return value != null ? value : def;
    }

    public Boolean getBooleanProperty(String name) {
        Object value = properties.get(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.valueOf((String) value);
        } else if (value != null) {
            return Boolean.valueOf(value.toString());
        } else {
            return null;
        }
    }

    public String getStringProperty(String name) {
        Object value = properties.get(name);
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    public boolean isFormRequest() {
        String contentHeader = getStringProperty(CONTENT_TYPE);
        return contentHeader == null || FORM_CONTENT_TYPE.equals(contentHeader);
    }

    public boolean hasProperty(String name) {
        return properties.get(name) != null;
    }

    @JsonIgnore
    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    public void setHeader(String name, Object value) {
        headers.put(name, value);
    }

    public String getHeader(String name) {
        Object value = headers.get(name);
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    @JsonIgnore
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public T getContents() {
        return contents;
    }
}
