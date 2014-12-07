package com.plexobject.encode.gson;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

import org.owasp.encoder.Encode;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.plexobject.encode.AbstractObjectCodec;
import com.plexobject.encode.CodecType;

public class GsonObjectCodec extends AbstractObjectCodec {
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static class GsonDateSerializer implements JsonDeserializer<Date>,
            JsonSerializer<Date>, InstanceCreator<Date> {
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc,
                JsonSerializationContext context) {
            return src == null ? null : new JsonPrimitive(src.getTime());
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return json == null ? null : new Date(json.getAsLong());
        }

        @Override
        public Date createInstance(Type typeOf) {
            return new Date();
        }
    }

    public static class GsonEncodingStringSerializer implements
            JsonDeserializer<String>, JsonSerializer<String>,
            InstanceCreator<String> {
        @Override
        public String deserialize(JsonElement json, Type typeOf,
                JsonDeserializationContext context) throws JsonParseException {
            return json.getAsJsonPrimitive().getAsString();
        }

        @Override
        public JsonElement serialize(String string, Type typeOf,
                JsonSerializationContext context) {
            return new JsonPrimitive(Encode.forXmlContent(string));
        }

        @Override
        public String createInstance(Type typeOf) {
            return "";
        }
    }

    private final Gson gson;

    public GsonObjectCodec() {
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(Date.class, new GsonDateSerializer())
                .registerTypeAdapter(String.class,
                        new GsonEncodingStringSerializer())
                .setDateFormat(TIMESTAMP_FORMAT)
                .setFieldNamingPolicy(
                        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
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
        return gson.toJson(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T decode(String text, Class<?> type, Map<String, Object> params) {
        if (text != null && text.length() > 0) {
            T obj = (T) gson.fromJson((String) text, type);

            populateProperties(params, obj);
            return obj;
        } else {
            return propertyDecode(params, type);
        }
    }

    @Override
    public CodecType getType() {
        return CodecType.JSON;
    }

}
