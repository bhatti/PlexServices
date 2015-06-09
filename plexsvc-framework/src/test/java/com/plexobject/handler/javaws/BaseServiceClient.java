package com.plexobject.handler.javaws;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.http.TestWebUtils;
import com.plexobject.util.ReflectUtils;

public class BaseServiceClient {
    public static final int DEFAULT_PORT = 8184;
    private static final ObjectCodec CODEC = new JsonObjectCodec();

    // private static final ObjectCodec CODEC = new XmlObjectCodec();

    @XmlRootElement
    public static class RequestBuilder {
        final Map<String, Object> request = new HashMap<>();

        public RequestBuilder(String method, Object payload) {
            request.put(method, payload);
        }

        public String encode() {
            return CODEC.encode(request);
        }

        @Override
        public String toString() {
            return encode();
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T post(String path, RequestBuilder request,
            Class<?> responseType, Type pType) throws Exception {
        System.out.println("SENDING " + request);
        String resp = TestWebUtils.post("http://localhost:" + DEFAULT_PORT
                + path, request.encode(), "Accept", "application/json");
        System.out.println("RECEIVED " + resp);
        int colon = resp.indexOf("\":");
        colon = resp.indexOf("\":", colon + 1);
        String payload = resp.substring(colon + 2, resp.length() - 2);

        return (T) ReflectUtils.decode(payload, responseType, pType, CODEC);
    }
}
