package com.plexobject.handler.javaws;

import java.lang.reflect.InvocationTargetException;
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

    private final Object delegate;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, JavawsServiceMethod> methodsByName = new HashMap<>();
    private JavawsServiceMethod defaultMethodInfo;

    public JavawsDelegateHandler(Object delegate, ServiceRegistry registry) {
        this.delegate = delegate;
        this.serviceRegistry = registry;
    }

    public void addMethod(JavawsServiceMethod info) {
        methodsByName.put(info.iMethod.getName(), info);
        defaultMethodInfo = info;
    }

    @Override
    public void handle(final Request<Object> request) {
        Pair<String, String> methodAndPayload = getMethodNameAndPayload(request);

        final JavawsServiceMethod methodInfo = methodsByName
                .get(methodAndPayload.first);
        if (methodInfo == null) {
            throw new ServiceInvocationException("Unknown method "
                    + methodAndPayload.first + ", available "
                    + methodsByName.keySet() + ", request "
                    + request.getPayload(), HttpResponse.SC_NOT_FOUND);
        }
        // set method name
        request.setMethodName(methodInfo.iMethod.getName());
        //
        final String responseTag = methodAndPayload.first + RESPONSE_SUFFIX;
        try {
            // make sure you use iMethod to decode because implMethod might have
            // erased parameterized type
            // We can get input parameters either from JSON text, form/query
            // parameters or method simply takes Map so we just pass all request
            // properties
            final Object[] args = methodInfo.useNameParams() ? ReflectUtils
                    .decode(methodInfo.iMethod, methodInfo.paramNames,
                            request.getProperties())
                    : methodAndPayload.second == null
                            && methodInfo.useMapProperties() ? new Object[] { request
                            .getProperties() } : ReflectUtils.decode(
                            methodAndPayload.second, methodInfo.iMethod,
                            request.getCodec());
            invokeWithAroundInterceptorIfNeeded(request, methodInfo,
                    responseTag, args);
        } catch (Exception e) {
            logger.error("PLEXSVC Failed to invoke " + methodInfo.iMethod
                    + ", for request " + request, e);
            request.getResponse().setPayload(e);
        }
    }

    private void invokeWithAroundInterceptorIfNeeded(
            final Request<Object> request,
            final JavawsServiceMethod methodInfo, final String responseTag,
            final Object[] args) throws Exception {
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

    private void invoke(Request<Object> request,
            final JavawsServiceMethod methodInfo, String responseTag,
            Object[] args) throws Exception {
        if (serviceRegistry.getSecurityAuthorizer() != null) {
            serviceRegistry.getSecurityAuthorizer().authorize(request, null);
        }
        try {
            Map<String, Object> response = new HashMap<>();
            Object result = methodInfo.implMethod.invoke(delegate, args);
            if (result != null) {
                response.put(responseTag, result);
            } else {
                response.put(responseTag, EMPTY_MAP);
            }
            request.getResponse().setPayload(response);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    // hard coding to handle JSON messages
    // TODO handle XML messages
    private Pair<String, String> getMethodNameAndPayload(Request<Object> request) {
        String text = request.getPayload();
        if (text == null || text.length() == 0 || text.charAt(0) != '{') {
            String method = request.getStringProperty("methodName");
            if (method == null) {
                if (methodsByName.size() == 1) {
                    return Pair.of(defaultMethodInfo.iMethod.getName(), null);
                }
                //
                throw new IllegalArgumentException("Unsupported request "
                        + request.getProperties());
            }
            //
            return Pair.of(method, null);
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
