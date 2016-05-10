package com.plexobject.handler.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.plexobject.handler.Request;

class MethodPayLoadRequest {
    private static final Logger logger = Logger
            .getLogger(MethodPayLoadRequest.class);

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    List<MethodPayLoadInfo> requests = new ArrayList<>();
    boolean multiRequest;

    @VisibleForTesting
    static MethodPayLoadRequest getMethodNameAndPayloads(Request request,
            String defaultMethod) {
        final MethodPayLoadRequest methodPayLoadRequest = new MethodPayLoadRequest();
        String text = request.getContentsAs();
        if (text != null && text.length() > 0) {
            try {
                JsonNode rootNode = jsonMapper.readTree(text);
                if (rootNode.isObject()) {
                    parseMethodPayload(methodPayLoadRequest, rootNode);
                } else if (rootNode.isArray()) {
                    methodPayLoadRequest.multiRequest = true;
                    for (int i = 0; i < rootNode.size(); i++) {
                        JsonNode node = rootNode.get(i);
                        parseMethodPayload(methodPayLoadRequest, node);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to parse " + text, e);
                int startObject = text.indexOf('{');
                int endObject = text.lastIndexOf('}');
                int colonPos = text.indexOf(':');
                //
                if (startObject != -1 && endObject != -1 && colonPos != -1) {
                    int methodStart = incrWhileSkipWhitespacesAndQuotes(text,
                            startObject + 1);
                    int methodEnd = decrWhileSkipWhitespacesAndQuotes(text,
                            colonPos - 1);
                    String method = text.substring(methodStart, methodEnd + 1);
                    //
                    int payloadStart = incrWhileSkipWhitespacesAndQuotes(text,
                            colonPos + 1);
                    int payloadEnd = decrWhileSkipWhitespacesAndQuotes(text,
                            endObject - 1);
                    String payload = payloadStart <= payloadEnd ? text
                            .substring(payloadStart, payloadEnd + 1).trim()
                            : "";
                    MethodPayLoadInfo info = new MethodPayLoadInfo(method,
                            payload);
                    methodPayLoadRequest.requests.add(info);
                }
            }
        }
        //
        if (methodPayLoadRequest.requests.size() == 0) {
            // trying to find method from the parameters
            String method = request.getStringProperty("methodName");
            if (method == null) {
                method = defaultMethod;
            }
            //
            if (method != null) {
                MethodPayLoadInfo info = new MethodPayLoadInfo(method,
                        text == null ? text : text.trim());
                methodPayLoadRequest.requests.add(info);
            }
        }
        //
        if (methodPayLoadRequest.requests.size() == 0) {
            throw new IllegalArgumentException(
                    "Could not find method-name in request "
                            + request.getProperties());
        }
        return methodPayLoadRequest;
    }

    private static void parseMethodPayload(
            MethodPayLoadRequest methodPayLoadRequest, JsonNode node) {
        if (node.isObject()) {
            Iterator<String> it = node.fieldNames();
            if (it.hasNext()) {
                String methodName = it.next();
                JsonNode payloadNode = node.get(methodName);
                String payload = payloadNode.isTextual() ? payloadNode.asText()
                        : payloadNode.toString();
                MethodPayLoadInfo info = new MethodPayLoadInfo(methodName,
                        payload);
                methodPayLoadRequest.requests.add(info);
            }
        }
    }

    private static int incrWhileSkipWhitespacesAndQuotes(String text, int start) {
        while (Character.isWhitespace(text.charAt(start))
                || text.charAt(start) == '\'' || text.charAt(start) == '"') {
            start++;
        }
        return start;
    }

    private static int decrWhileSkipWhitespacesAndQuotes(String text, int end) {
        while (Character.isWhitespace(text.charAt(end))
                || text.charAt(end) == '\'' || text.charAt(end) == '"') {
            end--;
        }
        return end;
    }
}
