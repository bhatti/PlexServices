package com.plexobject.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private final Properties properties = new Properties();

    public Configuration(String propertyFile) throws IOException {
        this(getInputStream(propertyFile));
    }

    public Configuration(InputStream in) throws IOException {
        properties.putAll(System.getProperties());
        properties.load(in);
    }

    public String getProperty(final String key) {
        return getProperty(key, null);
    }

    public String getProperty(final String key, final String def) {
        return properties.getProperty(key, def);
    }

    public int getInteger(final String key) {
        return getInteger(key, 0);
    }

    public int getInteger(final String key, final int def) {
        return Integer.parseInt(getProperty(key, String.valueOf(def)));
    }

    public double getDouble(final String key) {
        return getDouble(key, 0);
    }

    public double getDouble(final String key, final double def) {
        return Double.valueOf(getProperty(key, String.valueOf(def)))
                .doubleValue();
    }

    public boolean getBoolean(final String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(final String key, final boolean def) {
        return Boolean.valueOf(getProperty(key, String.valueOf(def)))
                .booleanValue();
    }

    public long getLong(final String key) {
        return getLong(key, 0);
    }

    public long getLong(final String key, long def) {
        return Long.valueOf(getProperty(key, String.valueOf(def)));
    }

    private static InputStream getInputStream(String propertyFile)
            throws IOException {
        InputStream in = Configuration.class.getClassLoader()
                .getResourceAsStream(propertyFile);
        if (in == null) {
            in = new FileInputStream(propertyFile);
        }
        return in;
    }
}
