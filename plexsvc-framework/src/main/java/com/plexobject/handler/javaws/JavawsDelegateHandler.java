package com.plexobject.handler.javaws;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.domain.Pair;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.ServiceInvocationException;
import com.plexobject.util.ReflectUtils;

public class JavawsDelegateHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory
            .getLogger(JavawsDelegateHandler.class);
    private static final String RESPONSE_SUFFIX = "Response";

    private final Object delegate;
    private final String responseNamespace;
    private final Map<String, Method> methods = new HashMap<>();

    public JavawsDelegateHandler(Object delegate, Configuration config) {
        this.delegate = delegate;
        this.responseNamespace = config.getProperty(Constants.JAVAWS_NAMESPACE,
                "ns2:");
    }

    public void addMethod(Method m) {
        methods.put(m.getName(), m);
    }

    @Override
    public void handle(Request request) {
        Pair<String, String> methodAndPayload = getMethodNameAndPayload((String) request
                .getPayload());

        Method method = methods.get(methodAndPayload.first);
        if (method == null) {
            throw new ServiceInvocationException("Unknown method "
                    + methodAndPayload.first + ", request "
                    + request.getPayload(), HttpResponse.SC_NOT_FOUND);
        }
        WebParam webParam = method.getParameterTypes().length > 0 ? method
                .getParameterTypes()[0].getAnnotation(WebParam.class) : null;
        String responseItemTag = webParam == null ? "item" : webParam.name();
        //
        String responseTag = responseNamespace + methodAndPayload.first
                + RESPONSE_SUFFIX;
        try {
            Object[] args = ReflectUtils.decode(methodAndPayload.second,
                    method, request.getCodec());
            Map<String, Object> response = new HashMap<>();
            Object result = method.invoke(delegate, args);
            if (result != null) {
                Map<String, Object> item = new HashMap<>();
                item.put(responseItemTag, result);
                response.put(responseTag, item);
            } else {
                response.put(responseTag, new HashMap<String, Object>());
            }
            request.getResponseDispatcher().send(response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to invoke " + method + ", for request "
                    + request, e);
            request.getResponseDispatcher().send(e);
        }
    }

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
