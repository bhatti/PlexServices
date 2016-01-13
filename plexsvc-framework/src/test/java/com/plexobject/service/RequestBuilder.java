package com.plexobject.service;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.json.FilteringJsonCodecConfigurer;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.encode.json.NonFilteringJsonCodecWriter;
import com.plexobject.encode.xml.XmlObjectCodec;

@XmlRootElement
public class RequestBuilder {
    public static CodecType codecType = CodecType.JSON;
    public static boolean filtering;

    public static ObjectCodec getObjectCodec() {
        ObjectCodec codec = codecType == CodecType.JSON ? new JsonObjectCodec()
                : new XmlObjectCodec();
        if (filtering) {
            codec.setCodecConfigurer(new FilteringJsonCodecConfigurer());
            codec.setObjectCodecFilteredWriter(new NonFilteringJsonCodecWriter());
        }
        return codec;
    }

    public static String getAcceptHeader() {
        return codecType == CodecType.JSON ? "application/json"
                : "application/xml";
    }

    final Map<String, Object> request = new HashMap<>();

    public RequestBuilder() {

    }

    public RequestBuilder(String method, Object payload) {
        if (payload != null) {
            request.put(method, payload);
        }
    }

    public String encode() {
        if (request.size() > 0) {
            return getObjectCodec().encode(request);
        }
        return null;
    }

    @Override
    public String toString() {
        return encode();
    }
}
