package com.plexobject.bugger.service;

import java.util.List;
import java.util.Map;

import com.plexobject.domain.Pair;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.encode.xml.XmlObjectCodec;
import com.plexobject.http.TestWebUtils;
import com.plexobject.util.ReflectUtils;

public class HttpHelper {
    public static final int DEFAULT_PORT = 8181;
    public static CodecType codecType = CodecType.JSON;
    private static String cookie;

    protected static ObjectCodec getObjectCodec() {
        return codecType == CodecType.JSON ? new JsonObjectCodec()
                : new XmlObjectCodec();
    }

    protected static String getAcceptHeader() {
        return codecType == CodecType.JSON ? "application/json"
                : "application/xml";
    }

    protected static String encode(Object obj) {
        return obj == null ? null : getObjectCodec().encode(obj);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T postForm(String path, Map<String, Object> request,
            Class<?> responseType) throws Exception {
        // System.out.println("SENDING " + request);
        Pair<String, Map<String, List<String>>> respAndCookie = TestWebUtils
                .postForm("http://localhost:" + DEFAULT_PORT + path, request,
                        "Cookie", cookie == null ? "" : cookie);
        // System.out.println("RECEIVED " + resp);
        // System.out.println("POST FORM " + path + " " + respAndCookie.second);
        if (respAndCookie.second != null) {
            Map<String, List<String>> headers = respAndCookie.second;
            List<String> cookies = headers.get("Set-Cookie");
            if (cookies != null && cookies.size() > 0) {
                cookie = cookies.get(0);
            }
        }
        return (T) ReflectUtils.decode(respAndCookie.first, responseType, null,
                getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T post(String path, Object request,
            Class<?> responseType) throws Exception {
        Pair<String, Map<String, List<String>>> respAndCookie = TestWebUtils
                .post("http://localhost:" + DEFAULT_PORT + path,
                        encode(request), "Accept", getAcceptHeader(),
                        "Content-Type", "application/json", "Cookie",
                        cookie == null ? "" : cookie);
        if (respAndCookie.second != null) {
            Map<String, List<String>> headers = respAndCookie.second;
            List<String> cookies = headers.get("Set-Cookie");
            if (cookies != null && cookies.size() > 0) {
                cookie = cookies.get(0);
            }
        }
        // System.out.println("POST " + path + " " + respAndCookie.second);
        return (T) ReflectUtils.decode(respAndCookie.first, responseType, null,
                getObjectCodec());
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(String path, Class<?> responseType)
            throws Exception {
        // System.out.println("SENDING " + request);
        String resp = TestWebUtils.get("http://localhost:" + DEFAULT_PORT
                + path, "Accept", getAcceptHeader(), "Content-Type",
                "application/json", "Cookie", cookie == null ? "" : cookie);
        // System.out.println("RECEIVED " + resp);
        return (T) ReflectUtils.decode(resp, responseType, null,
                getObjectCodec());
    }

}
