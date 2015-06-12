package com.plexobject.handler.javaws;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.domain.Pair;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.ServiceInvocationException;
import com.plexobject.util.ReflectUtils;

public class JavawsDelegateHandler implements RequestHandler {
    private static final Logger logger = Logger
            .getLogger(JavawsDelegateHandler.class);
    private static final String RESPONSE_SUFFIX = "Response";

    public static class MethodInfo {
        public final Method iMethod;
        public final Method implMethod;
        public final String itemName;

        public MethodInfo(Method iMethod, Method implMethod, String itemName) {
            super();
            this.iMethod = iMethod;
            this.implMethod = implMethod;
            this.itemName = itemName;
        }
    }

    private final Object delegate;
    private final String responseNamespace;
    private final Map<String, MethodInfo> methodsByName = new HashMap<>();

    public JavawsDelegateHandler(Object delegate, Configuration config) {
        this.delegate = delegate;
        this.responseNamespace = config.getProperty(Constants.JAVAWS_NAMESPACE,
                "");
    }

    public void addMethod(MethodInfo info) {
        methodsByName.put(info.iMethod.getName(), info);
    }

    @Override
    public void handle(Request<Object> request) {
        Pair<String, String> methodAndPayload = getMethodNameAndPayload((String) request
                .getPayload());

        final MethodInfo methodInfo = methodsByName.get(methodAndPayload.first);
        if (methodInfo == null) {
            throw new ServiceInvocationException("Unknown method "
                    + methodAndPayload.first + ", request "
                    + request.getPayload(), HttpResponse.SC_NOT_FOUND);
        }
        //
        String responseTag = responseNamespace + methodAndPayload.first
                + RESPONSE_SUFFIX;
        try {
            // make sure you use iMethod to decode because implMethod might have
            // erased parameterized type
            Object[] args = ReflectUtils.decode(methodAndPayload.second,
                    methodInfo.iMethod, request.getCodec());
            Map<String, Object> response = new HashMap<>();
            Object result = methodInfo.implMethod.invoke(delegate, args);
            if (result != null) {
                if (methodInfo.itemName != null
                        && methodInfo.itemName.length() > 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put(methodInfo.itemName, result);
                    response.put(responseTag, item);
                } else {
                    response.put(responseTag, result);
                }
            } else {
                response.put(responseTag, new HashMap<String, Object>());
            }
            request.getResponse().setPayload(response);
        } catch (Exception e) {
            logger.error("Failed to invoke " + methodInfo.implMethod
                    + ", for request " + request, e);
            request.getResponse().setPayload(e);
        }
    }

    // hard coding to handle JSON messages
    // TODO handle XML messages
    private static Pair<String, String> getMethodNameAndPayload(String text) {
        if (text == null || text.length() == 0 || text.charAt(0) != '{') {
            throw new IllegalArgumentException("Unsupported request " + text);
        }
        int colon = text.indexOf(':');
        String method = text.substring(1, colon);
        if (method.charAt(0) == '"') {
            method = method.substring(1, method.length() - 1);
        }
        String payload = text.substring(colon + 1, text.length() - 1);
        return Pair.of(method, payload);
    }
}
