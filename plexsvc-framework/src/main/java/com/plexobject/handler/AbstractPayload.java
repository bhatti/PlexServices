package com.plexobject.handler;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractPayload {
    private final Map<String, Object> properties;
    private final long createdAt;
    private final Object payload;

    public AbstractPayload(final Map<String, Object> properties,
            final Object payload) {
        this.createdAt = System.currentTimeMillis();
        this.properties = properties;
        this.payload = payload;
    }

    @SuppressWarnings("unchecked")
    public <V> V getProperty(String name) {
        return (V) properties.get(name);
    }

    public Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @SuppressWarnings("unchecked")
    public <T> T getPayload() {
        return (T) payload;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [properties=" + properties
                + ", payload=" + payload + "]";
    }

}
