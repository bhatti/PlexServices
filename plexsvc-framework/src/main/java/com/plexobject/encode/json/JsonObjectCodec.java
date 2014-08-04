package com.plexobject.encode.json;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.plexobject.encode.ObjectCodec;

public class JsonObjectCodec implements ObjectCodec {
    private static final Logger log = LoggerFactory
            .getLogger(JsonObjectCodec.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public JsonObjectCodec() {
        // mapper.enableDefaultTyping();
        // mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
        // JsonTypeInfo.As.WRAPPER_OBJECT);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
    }

    @Override
    public <T> String encode(T obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert " + obj, e);
        }
    }

    @Override
    public <T> T decode(String text, Class<?> type, Map<String, Object> params) {
        if (text != null && text.length() > 0) {
            T obj = jsonDecode(text, type);
            populateProperties(params, obj);
            return obj;
        } else {
            return propertyDecode(params, type);
        }
    }

    public <T> T decode(String text, final TypeReference<T> type) {
        try {
            return mapper.readValue(text, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode " + text, e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T propertyDecode(Map<String, Object> params, Class<?> type) {
        try {
            T object = (T) type.newInstance();
            populateProperties(params, object);
            return object;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to decode " + params, ex);
        }
    }

    private <T> void populateProperties(Map<String, Object> params, T object) {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            try {
                String name = toCamelCase(e.getKey());
                BeanUtils.setProperty(object, name, e.getValue());
            } catch (Exception ex) {
                log.warn("Failed to set " + e.getKey() + "=>" + e.getValue(),
                        ex);
            }
        }
    }

    private static String toCamelCase(String s) {
        String[] parts = s.split("_");
        if (parts.length < 2) {
            return s;
        }
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            sb.append(toProperCase(parts[i]));
        }
        return sb.toString();
    }

    private static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private <T> T jsonDecode(String text, Class<?> type) {
        ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    getClass().getClassLoader());
            return (T) mapper.readValue(text, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode '" + text + "'", e);
        } finally {
            Thread.currentThread().setContextClassLoader(savedCL);
        }
    }
}
