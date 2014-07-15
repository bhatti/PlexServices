package com.plexobject.bus;

import java.util.Map;

/**
 * This class encapsulates event
 * 
 * @author shahzad bhatti
 *
 */
public class Event {
    private final Map<String, Object> properties;
    private final Object contents;
    private final long created = System.currentTimeMillis();

    public Event(Map<String, Object> properties, Object contents) {
        this.properties = properties;
        this.contents = contents;
    }

    /**
     * This method returns source of event
     * 
     * @param name
     *            - name of property
     * @return value of property
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name) {
        return (T) properties.get(name);
    }

    /**
     * This method returns contents of event
     * 
     * @return contents
     */
    @SuppressWarnings("unchecked")
    public <T> T getContents() {
        return (T) contents;
    }

    /**
     * This method returns time when event was created
     * 
     * @return timestamp
     */
    public long getCreatedAt() {
        return created;
    }
}
