package com.plexobject.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.domain.Statusable;
import com.plexobject.http.HttpResponse;

public abstract class AbstractPayload {
    protected final Map<String, Object> properties;
    protected final Map<String, Object> headers;
    protected final long createdAt;
    protected Object payload;
    protected int status = 0;

    AbstractPayload() {
        this.createdAt = System.currentTimeMillis();
        this.properties = new HashMap<>();
        this.headers = new HashMap<>();
    }

    protected AbstractPayload(final Map<String, Object> properties,
            final Map<String, Object> headers, final Object payload) {
        this.createdAt = System.currentTimeMillis();
        this.properties = properties;
        this.headers = headers;
        this.payload = payload;
    }

    public void setPayload(Object obj) {
        this.payload = obj;
        if (payload instanceof Statusable) {
            setStatus(((Statusable) payload).getStatus());
        }
    }

    public int getStatus() {
        if (status > 0) {
            return status;
        } else {
            Object value = properties.get(HttpResponse.STATUS);
            if (value != null) {
                if (value instanceof Number) {
                    setStatus(((Number) value).intValue());
                    return status;
                } else if (value instanceof String) {
                    setStatus(Integer.parseInt((String) value));
                    return status;
                }
            }
        }
        return HttpResponse.SC_OK;
    }

    public AbstractPayload setStatus(int status) {
        this.status = status;
        properties.put(HttpResponse.STATUS, status);
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
        String contentHeader = getStringProperty("Content-Type");
        return contentHeader == null
                || "application/x-www-form-urlencoded".equals(contentHeader);
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

    @SuppressWarnings("unchecked")
    public <T> T getPayload() {
        return (T) payload;
    }

}
