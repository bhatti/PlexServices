package com.plexobject.encode.json;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.plexobject.encode.AbstractObjectCodec;
import com.plexobject.encode.CodecConfigurer;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.EncodingException;
import com.plexobject.encode.ObjectCodecFilteredWriter;

/**
 * This class implements codec for JSON
 * 
 * @author shahzad bhatti
 *
 */
public class JsonObjectCodec extends AbstractObjectCodec {
    private static ThreadLocal<ObjectMapper> currentMapper = new ThreadLocal<ObjectMapper>() {
        @Override
        protected ObjectMapper initialValue() {
            final ObjectMapper mapper = new ObjectMapper();
            // mapper.enableDefaultTyping();
            // mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
            // JsonTypeInfo.As.WRAPPER_OBJECT);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.setSerializationInclusion(Include.NON_NULL);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);
            SimpleModule module = new SimpleModule();
            // module.addSerializer(Throwable.class, new ExceptionSerializer());
            mapper.registerModule(module);
            //
            return mapper;
        }
    };

    public JsonObjectCodec() {
    }

    @Override
    public void setCodecConfigurer(CodecConfigurer codecConfigurer) {
        super.setCodecConfigurer(codecConfigurer);
        if (codecConfigurer != null) {
            codecConfigurer.configureCodec(currentMapper.get());
        }
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
            final ObjectCodecFilteredWriter writer = getObjectCodecFilteredWriter();
            if (writer != null) {
                return writer.writeString(currentMapper.get(), obj);
            } else {
                return currentMapper.get().writeValueAsString(obj);
            }
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
        if (text == null || text.length() == 0) {
            return null;
        }
        try {
            return currentMapper.get().readValue(text, type);
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
            return (T) currentMapper.get().readValue(text, type);
        } catch (IOException e) {
            throw new EncodingException("Failed to decode '" + text + "' to "
                    + type, e);
        } finally {
            Thread.currentThread().setContextClassLoader(savedCL);
        }
    }
}
