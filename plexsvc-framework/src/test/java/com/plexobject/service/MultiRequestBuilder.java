package com.plexobject.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.json.FilteringJsonCodecConfigurer;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.encode.json.NonFilteringJsonCodecWriter;
import com.plexobject.encode.xml.XmlObjectCodec;

@XmlRootElement
public class MultiRequestBuilder {
    public static CodecType codecType = CodecType.JSON;
    private static final ObjectMapper jsonMapper = new ObjectMapper();
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

    final List<Map<String, Object>> requests = new ArrayList<>();

    public MultiRequestBuilder() {

    }

    public MultiRequestBuilder(String method, Object payload) {
        add(method, payload);
    }

    public void add(String method, Object payload) {
        if (payload != null) {
            Map<String, Object> request = new HashMap<>();
            request.put(method, payload);
            requests.add(request);
        }
    }

    public String encode() {
        return getObjectCodec().encode(requests);
    }

    @Override
    public String toString() {
        return encode();
    }

    public static Map<String, String> parseResponseObject(String json) {
        Map<String, String> response = new HashMap<>();
        try {
            JsonNode rootNode = jsonMapper.readTree(json);
            if (rootNode.isObject()) {
                parseResponseObject(response, rootNode);
            } else if (rootNode.isArray()) {
                for (int i = 0; i < rootNode.size(); i++) {
                    JsonNode node = rootNode.get(i);
                    parseResponseObject(response, node);
                }
            }
        } catch (IOException e) {
        }

        return response;
    }

    private static void parseResponseObject(Map<String, String> response,
            JsonNode node) {
        if (node.isObject() && node.size() == 1) {
            Iterator<String> it = node.fieldNames();
            if (it.hasNext()) {
                String key = it.next();
                if (response.containsKey(key)) {
                    String value = response.get(key);
                    response.put(key, value + "___" + node.get(key).toString());
                } else {
                    response.put(key, node.get(key).toString());
                }
            }
        } else {
            response.put(node.asText(), node.toString());
        }
    }
}