package com.plexobject.encode.json;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
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
            return jsonDecode(text, type);
        } else {
            return propertyDecode(params, type);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T propertyDecode(Map<String, Object> params, Class<?> type) {
        try {
            T object = (T) type.newInstance();
            for (Map.Entry<String, Object> e : params.entrySet()) {
                try {
                    BeanUtils.setProperty(object, e.getKey(), e.getValue());
                } catch (Exception ex) {
                    log.warn(
                            "Failed to set " + e.getKey() + "=>" + e.getValue(),
                            ex);
                }
            }
            return object;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to decode " + params, ex);
        }
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
