package com.plexobject.service;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.encode.xml.XmlObjectCodec;
import com.plexobject.http.TestWebUtils;
import com.plexobject.util.ReflectUtils;

public class BaseServiceClient {
    public static final int DEFAULT_PORT = 8186;
    public static CodecType codecType = CodecType.JSON;

    protected static ObjectCodec getObjectCodec() {
        return codecType == CodecType.JSON ? new JsonObjectCodec()
                : new XmlObjectCodec();
    }

    @XmlRootElement
    public static class RequestBuilder {
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

    protected static String getAcceptHeader() {
        return codecType == CodecType.JSON ? "application/json"
                : "application/xml";
    }

    @SuppressWarnings("unchecked")
    protected <T> T post(String path, RequestBuilder request,
            Class<?> responseType, Type pType) throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.post("http://localhost:" + DEFAULT_PORT
                + path, request.encode(), "Accept", getAcceptHeader()).first;
        // System.out.println("RECEIVED " + resp);
        int colon = resp.indexOf("\":");
        String payload = resp.substring(colon + 2, resp.length() - 1);

        return (T) ReflectUtils.decode(payload, responseType, pType,
                getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T post(String path, Object input, Class<?> responseType)
            throws Exception {
        String jsonInput = getObjectCodec().encode(input);
        String resp = TestWebUtils.post("http://localhost:" + DEFAULT_PORT
                + path, jsonInput, "Accept", getAcceptHeader()).first;
        return (T) ReflectUtils.decode(resp, responseType, null,
                getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(String path, Class<?> responseType, Type pType)
            throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", getAcceptHeader());
        // System.out.println("RECEIVED " + resp);
        int colon = resp.indexOf("\":");
        String payload = resp.substring(colon + 2, resp.length() - 1);

        return (T) ReflectUtils.decode(payload, responseType, pType,
                getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(String path, Class<?> responseType)
            throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", getAcceptHeader());
        // System.out.println("RECEIVED " + resp);
        return (T) ReflectUtils.decode(resp, responseType, null,
                getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getWithHeader(String path, Class<?> responseType,
            String headerKey, String headerValue) throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", getAcceptHeader(), headerKey, headerValue);
        // System.out.println("RECEIVED " + resp);
        return (T) ReflectUtils.decode(resp, responseType, null,
                getObjectCodec());
    }
}
