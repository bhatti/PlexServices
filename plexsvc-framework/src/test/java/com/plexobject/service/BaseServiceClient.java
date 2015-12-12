package com.plexobject.service;

import java.lang.reflect.Type;

import com.plexobject.http.TestWebUtils;
import com.plexobject.util.ReflectUtils;

public class BaseServiceClient {
    public static final int DEFAULT_PORT = 8186;

    @SuppressWarnings("unchecked")
    protected <T> T post(String path, RequestBuilder request,
            Class<?> responseType, Type pType) throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.post("http://localhost:" + DEFAULT_PORT
                + path, request.encode(), "Accept", RequestBuilder.getAcceptHeader()).first;
        //System.out.println("RECEIVED " + resp);
        int colon = resp.indexOf("\":");
        String payload = resp.substring(colon + 2, resp.length() - 1);
        return (T) ReflectUtils.decode(payload, responseType, pType,
                RequestBuilder.getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T post(String path, Object input, Class<?> responseType)
            throws Exception {
        String jsonInput = RequestBuilder.getObjectCodec().encode(input);
        String resp = TestWebUtils.post("http://localhost:" + DEFAULT_PORT
                + path, jsonInput, "Accept", RequestBuilder.getAcceptHeader()).first;
        return (T) ReflectUtils.decode(resp, responseType, null,
                RequestBuilder.getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(String path, Class<?> responseType, Type pType)
            throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", RequestBuilder.getAcceptHeader());
        // System.out.println("RECEIVED " + resp);
        int colon = resp.indexOf("\":");
        String payload = resp.substring(colon + 2, resp.length() - 1);

        return (T) ReflectUtils.decode(payload, responseType, pType,
                RequestBuilder.getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(String path, Class<?> responseType)
            throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", RequestBuilder.getAcceptHeader());
        // System.out.println("RECEIVED " + resp);
        return (T) ReflectUtils.decode(resp, responseType, null,
                RequestBuilder.getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getWithHeader(String path, Class<?> responseType,
            String headerKey, String headerValue) throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", RequestBuilder.getAcceptHeader(), headerKey, headerValue);
        // System.out.println("RECEIVED " + resp);
        return (T) ReflectUtils.decode(resp, responseType, null,
                RequestBuilder.getObjectCodec());
    }
}
