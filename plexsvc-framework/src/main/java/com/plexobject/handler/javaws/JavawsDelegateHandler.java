package com.plexobject.handler.javaws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebParam;

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

    private final Object delegate;
    private final String responseNamespace;
    private final Map<String, Pair<Method, Method>> methodsByName = new HashMap<>();

    public JavawsDelegateHandler(Object delegate, Configuration config) {
        this.delegate = delegate;
        this.responseNamespace = config.getProperty(Constants.JAVAWS_NAMESPACE,
                "ns2:");
    }

    public void addMethod(Method iMethod, Method implMethod) {
        methodsByName.put(iMethod.getName(), Pair.of(iMethod, implMethod));
    }

    @Override
    public void handle(Request<Object> request) {
        Pair<String, String> methodAndPayload = getMethodNameAndPayload((String) request
                .getPayload());

        final Pair<Method, Method> methods = methodsByName
                .get(methodAndPayload.first);
        if (methods == null) {
            throw new ServiceInvocationException("Unknown method "
                    + methodAndPayload.first + ", request "
                    + request.getPayload(), HttpResponse.SC_NOT_FOUND);
        }
        final Method iMethod = methods.first;
        final Method implMethod = methods.second;
        String responseItemTag = getItemTag(iMethod, implMethod);
        //
        String responseTag = responseNamespace + methodAndPayload.first
                + RESPONSE_SUFFIX;
        try {
            Object[] args = ReflectUtils.decode(methodAndPayload.second,
                    iMethod, request.getCodec());
            Map<String, Object> response = new HashMap<>();
            Object result = implMethod.invoke(delegate, args);
            if (result != null) {
                Map<String, Object> item = new HashMap<>();
                item.put(responseItemTag, result);
                response.put(responseTag, item);
            } else {
                response.put(responseTag, new HashMap<String, Object>());
            }
            request.getResponse().setPayload(response);
        } catch (Exception e) {
            logger.error("Failed to invoke " + implMethod + ", for request "
                    + request, e);
            request.getResponse().setPayload(e);
        }
    }

    private String getItemTag(final Method iMethod, final Method implMethod) {
        WebParam webParam = getWebParamFor(iMethod);
        if (webParam == null) {
            webParam = getWebParamFor(implMethod);
        }
        String responseItemTag = webParam != null ? webParam.name() : "item";
        return responseItemTag;
    }

    private WebParam getWebParamFor(final Method iMethod) {
        for (Annotation[] annotations : iMethod.getParameterAnnotations()) {
            for (Annotation a : annotations) {
                if (a instanceof WebParam) {
                    return (WebParam) a;
                }
            }
        }
        return null;
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
