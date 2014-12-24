package com.plexobject.encode.json;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.plexobject.encode.AbstractObjectCodec;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.EncodingException;

/**
 * This class implements codec for JSON
 * 
 * @author shahzad bhatti
 *
 */
public class JsonObjectCodec extends AbstractObjectCodec {
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
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof CharSequence) {
            return obj.toString();
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new EncodingException("Failed to convert " + obj, e);
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
        } catch (IOException e) {
            throw new EncodingException("Failed to decode " + text, e);
        }
    }

    @Override
    public CodecType getType() {
        return CodecType.JSON;
    }

    @SuppressWarnings("unchecked")
    <T> T jsonDecode(String text, Class<?> type) {
        if (text == null || text.length() == 0) {
            return null;
        }
        ClassLoader savedCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(
                    getClass().getClassLoader());
            return (T) mapper.readValue(text, type);
        } catch (IOException e) {
            throw new EncodingException("Failed to decode " + text.length()
                    + ", '" + text + "'", e);
        } finally {
            Thread.currentThread().setContextClassLoader(savedCL);
        }
    }
}
