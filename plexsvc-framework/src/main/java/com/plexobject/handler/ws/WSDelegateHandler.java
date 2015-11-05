package com.plexobject.handler.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.plexobject.domain.Pair;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.ServiceInvocationException;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;
import com.plexobject.util.ReflectUtils.ParamType;

public class WSDelegateHandler implements RequestHandler {
    private static final Logger logger = Logger
            .getLogger(WSDelegateHandler.class);
    private static final String RESPONSE_SUFFIX = "Response";

    private final Object delegate;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, WSServiceMethod> methodsByName = new HashMap<>();
    private WSServiceMethod defaultMethodInfo;

    public WSDelegateHandler(Object delegate, ServiceRegistry registry) {
        this.delegate = delegate;
        this.serviceRegistry = registry;
    }

    public void addMethod(WSServiceMethod info) {
        methodsByName.put(info.iMethod.getName(), info);
        defaultMethodInfo = info;
    }

    @Override
    public void handle(final Request request) {
        Pair<String, String> methodAndPayload = getMethodNameAndPayload(request);

        final WSServiceMethod methodInfo = methodsByName
                .get(methodAndPayload.first);
        if (methodInfo == null) {
            logger.warn("PLEXSVC: Unknown method '" + methodAndPayload.first
                    + "', available " + methodsByName.keySet() + ", request "
                    + request.getContents());
            throw new ServiceInvocationException("Unknown method '"
                    + methodAndPayload.first + "'" + methodsByName.keySet(),
                    HttpResponse.SC_NOT_FOUND);
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
            if (methodInfo.hasMultipleParamTypes) {
                for (int i = 0; i < methodInfo.params.length; i++) {
                    if (methodInfo.params[i].type == ParamType.MAP_PARAM) {
                        methodInfo.params[i].defaultValue = request
                                .getPropertiesAndHeaders();
                    } else if (methodInfo.params[i].type == ParamType.REQUEST_PARAM) {
                        methodInfo.params[i].defaultValue = request;
                    }
                }
            }
            //
            final Object[] args = ReflectUtils.decode(methodInfo.iMethod,
                    request.getPropertiesAndHeaders(), methodInfo.params,
                    methodAndPayload.second, request.getCodec());

            invokeWithAroundInterceptorIfNeeded(request, methodInfo,
                    responseTag, args);
        } catch (Exception e) {
            logger.error("PLEXSVC Failed to invoke " + methodInfo.iMethod
                    + ", for request " + request, e);
            request.getResponse().setContents(e);
        }
    }

    @Override
    public String toString() {
        return "WSDelegateHandler [delegate=" + delegate + "]";
    }

    private void invokeWithAroundInterceptorIfNeeded(final Request request,
            final WSServiceMethod methodInfo, final String responseTag,
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

    private void invoke(Request request, final WSServiceMethod methodInfo,
            String responseTag, Object[] args) throws Exception {
        if (serviceRegistry.getSecurityAuthorizer() != null) {
            serviceRegistry.getSecurityAuthorizer().authorize(request, null);
        }
        try {
            Object result = methodInfo.implMethod.invoke(delegate, args);
            if (logger.isDebugEnabled()) {
                logger.debug("****PLEXSVC MLN Invoking "
                        + methodInfo.iMethod.getName() + " with "
                        + Arrays.toString(args) + ", result " + result);
            }
            if (request.getResponse().getContents() == null) {
                Map<String, Object> response = new HashMap<>();
                if (result != null) {
                    response.put(responseTag, result);
                } else {
                    response.put(responseTag, null);
                }
                request.getResponse().setContents(response);
            }
        } catch (InvocationTargetException invocationTargetException) {
            Exception e = invocationTargetException;
            if (e.getCause() instanceof Exception) { // TODO can this be while
                e = (Exception) e.getCause();
            }
            throw e;
        } catch (Exception e) {
            throw e;
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

    @VisibleForTesting
    Pair<String, String> getMethodNameAndPayload(Request request) {
        String text = request.getContentsAs();
        // hard coding to handle JSON messages
        // manual parsing because I don't want to run complete JSON parser
        int startObject = text != null ? text.indexOf('{') : -1;
        int endObject = text != null ? text.lastIndexOf('}') : -1;
        int colonPos = text.indexOf(':');

        if (text == null || startObject == -1 || endObject == -1
                || colonPos == -1) {
            String method = request.getStringProperty("methodName");
            if (method == null) {
                if (methodsByName.size() == 1) {
                    method = defaultMethodInfo.iMethod.getName();
                } else {
                    throw new IllegalArgumentException(
                            "Could not find method-name in request "
                                    + request.getProperties());
                }
            }
            //
            return Pair.of(method, text == null ? text : text.trim());
        }
        //
        int methodStart = incrWhileSkipWhitespacesAndQuotes(text,
                startObject + 1);
        int methodEnd = decrWhileSkipWhitespacesAndQuotes(text, colonPos - 1);
        String method = text.substring(methodStart, methodEnd + 1);
        //
        int payloadStart = incrWhileSkipWhitespacesAndQuotes(text, colonPos + 1);
        int payloadEnd = decrWhileSkipWhitespacesAndQuotes(text, endObject - 1);
        String payload = payloadStart <= payloadEnd ? text.substring(
                payloadStart, payloadEnd + 1).trim() : "";
        return Pair.of(method, payload);
    }
}
