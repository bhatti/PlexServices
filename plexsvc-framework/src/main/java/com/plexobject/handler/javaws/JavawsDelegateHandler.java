package com.plexobject.handler.javaws;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.plexobject.domain.Pair;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.ServiceInvocationException;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;

public class JavawsDelegateHandler implements RequestHandler {
    private static final Logger logger = Logger
            .getLogger(JavawsDelegateHandler.class);
    private static final String RESPONSE_SUFFIX = "Response";
    private static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    public static class MethodInfo {
        public final Method iMethod;
        public final Method implMethod;
        public final String itemName;

        public MethodInfo(Method iMethod, Method implMethod, String itemName) {
            this.iMethod = iMethod;
            this.implMethod = implMethod;
            this.itemName = itemName;
        }
    }

    private final Object delegate;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, MethodInfo> methodsByName = new HashMap<>();

    public JavawsDelegateHandler(Object delegate, ServiceRegistry registry) {
        this.delegate = delegate;
        this.serviceRegistry = registry;
    }

    public void addMethod(MethodInfo info) {
        methodsByName.put(info.iMethod.getName(), info);
    }

    @Override
    public void handle(final Request<Object> request) {
        Pair<String, String> methodAndPayload = getMethodNameAndPayload((String) request
                .getPayload());

        final MethodInfo methodInfo = methodsByName.get(methodAndPayload.first);
        if (methodInfo == null) {
            throw new ServiceInvocationException("Unknown method "
                    + methodAndPayload.first + ", request "
                    + request.getPayload(), HttpResponse.SC_NOT_FOUND);
        }
        // set method name
        request.setMethodName(methodInfo.iMethod.getName());
        //
        final String responseTag = methodAndPayload.first + RESPONSE_SUFFIX;
        try {
            // make sure you use iMethod to decode because implMethod might have
            // erased parameterized type
            final Object[] args = ReflectUtils.decode(methodAndPayload.second,
                    methodInfo.iMethod, request.getCodec());
            invokeWithAroundInterceptorIfNeeded(request, methodInfo,
                    responseTag, args);
        } catch (Exception e) {
            logger.error("Failed to invoke " + methodInfo.iMethod
                    + ", for request " + request, e);
            request.getResponse().setPayload(e);
        }
    }

    private void invokeWithAroundInterceptorIfNeeded(
            final Request<Object> request, final MethodInfo methodInfo,
            final String responseTag, final Object[] args) throws Exception {
        if (serviceRegistry.getAroundInterceptor() != null) {
            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    invoke(request, methodInfo, responseTag, args);
                    return null;
                }
            };
            serviceRegistry.getAroundInterceptor().proceed(delegate,
                    methodInfo.iMethod.getName(), callable);
        } else {
            invoke(request, methodInfo, responseTag, args);
        }
    }

    private void invoke(Request<Object> request, final MethodInfo methodInfo,
            String responseTag, Object[] args) throws Exception {
        if (serviceRegistry.getSecurityAuthorizer() != null) {
            serviceRegistry.getSecurityAuthorizer().authorize(request, null);
        }
        Map<String, Object> response = new HashMap<>();
        Object result = methodInfo.implMethod.invoke(delegate, args);

        if (result != null) {
            if (methodInfo.itemName != null && methodInfo.itemName.length() > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put(methodInfo.itemName, result);
                response.put(responseTag, item);
            } else {
                response.put(responseTag, result);
            }
        } else {
            response.put(responseTag, EMPTY_MAP);
        }
        request.getResponse().setPayload(response);
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
