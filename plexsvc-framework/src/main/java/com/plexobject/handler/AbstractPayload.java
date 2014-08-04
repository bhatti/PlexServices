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

	public boolean hasProperty(String name) {
		return properties.get(name) != null;
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
