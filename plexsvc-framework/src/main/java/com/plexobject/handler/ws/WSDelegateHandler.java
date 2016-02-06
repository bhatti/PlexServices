package com.plexobject.handler.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.plexobject.domain.Pair;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.ServiceInvocationException;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;
import com.plexobject.util.ReflectUtils.ParamType;

public class WSDelegateHandler implements RequestHandler {
    private static final Logger logger = Logger
            .getLogger(WSDelegateHandler.class);
    private static final String RESPONSE_SUFFIX = "Response";
    private static final ObjectMapper jsonMapper = new ObjectMapper();
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
        boolean[] multiRequest = new boolean[1];
        List<Pair<String, String>> methodAndPayloads = getMethodNameAndPayloads(
                request, multiRequest);
        if (methodAndPayloads.size() == 0) {
            throw new IllegalArgumentException(
                    "Could not find method-name in request "
                            + request.getProperties());
        }
        final List<Object> multiRequestResponse = new ArrayList<>();

        for (int n = 0; n < methodAndPayloads.size(); n++) {
            final Pair<String, String> methodAndPayload = methodAndPayloads
                    .get(n);
            final String responseTag = methodAndPayload.first + RESPONSE_SUFFIX;

            //
            final WSServiceMethod methodInfo = methodsByName
                    .get(methodAndPayload.first);
            try {
                if (methodInfo == null) {
                    logger.warn("PLEXSVC: Unknown method '"
                            + methodAndPayload.first + "', available "
                            + methodsByName.keySet() + ", request "
                            + request.getContents());
                    throw new ServiceInvocationException("Unknown method '"
                            + methodAndPayload.first + "'",
                            multiRequest[0] ? HttpResponse.SC_OK
                                    : HttpResponse.SC_NOT_FOUND);
                }
                // set method name
                request.setMethodName(methodInfo.iMethod.getName());
                //
                // make sure you use iMethod to decode because implMethod might
                // have
                // erased parameterized type
                // We can get input parameters either from JSON text, form/query
                // parameters or method simply takes Map so we just pass all
                // request
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
                if (args.length > 0) {
                    request.setContents(args[0]); // In most cases the first
                                                  // argument will be the
                                                  // decoded
                                                  // object
                }
                invokeWithAroundInterceptorIfNeeded(request, methodInfo,
                        responseTag, args);
            } catch (Exception e) {
                logger.error("PLEXSVC Failed to invoke "
                        + (methodInfo != null ? methodInfo.iMethod
                                : " unknown-method") + ", for request "
                        + request, e);
                request.getResponse().setContents(e);
            }
            if (multiRequest[0]) {
                if (request.getResponse().getContents() instanceof Exception) {
                    Map<String, Object> response = new HashMap<String, Object>();
                    response.put(responseTag, request.getResponse()
                            .getContents());
                    multiRequestResponse.add(response);
                } else {
                    multiRequestResponse.add(request.getResponse()
                            .getContents());
                }
                request.getResponse().setContents(null);// so that we can
                                                        // overwrite it in
                                                        // invoke otherwise it
                                                        // assumes service set
                                                        // the contents
            }
        }
        if (multiRequest[0]) {
            request.getResponse().setContents(multiRequestResponse);
        }
    }

    @Override
    public String toString() {
        return "WSDelegateHandler [delegate=" + delegate + "]";
    }

    private void invokeWithAroundInterceptorIfNeeded(Request request,
            final WSServiceMethod methodInfo, final String responseTag,
            final Object[] args) throws Exception {
        if (serviceRegistry.getAroundInterceptor() != null) {
            final Request inputRequest = request;
            Callable<Object> callable = new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Request request = requestAfterRequestInterceptors(inputRequest);
                    invoke(request, methodInfo, responseTag, args);
                    return null;
                }
            };
            serviceRegistry.getAroundInterceptor().proceed(delegate,
                    methodInfo.iMethod.getName(), callable);
        } else {
            request = requestAfterRequestInterceptors(request);
            invoke(request, methodInfo, responseTag, args);
        }
    }

    private Request requestAfterRequestInterceptors(Request request) {
        if (serviceRegistry.hasRequestInterceptors()) {
            for (Interceptor<Request> interceptor : serviceRegistry
                    .getRequestInterceptors()) {
                request = interceptor.intercept(request);
            }
        }
        return request;
    }

    private void invoke(final Request request,
            final WSServiceMethod methodInfo, final String responseTag,
            final Object[] args) throws Exception {
        if (serviceRegistry.getSecurityAuthorizer() != null) {
            serviceRegistry.getSecurityAuthorizer().authorize(request, null);
        }
        try {
            Object result = methodInfo.implMethod.invoke(delegate, args);
            if (logger.isDebugEnabled()) {
                logger.debug("****PLEXSVC Invoking "
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
                //
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

    @VisibleForTesting
    List<Pair<String, String>> getMethodNameAndPayloads(Request request,
            boolean[] multiRequest) {
        String text = request.getContentsAs();
        List<Pair<String, String>> response = new ArrayList<>();
        if (text != null && text.length() > 0) {
            try {
                JsonNode rootNode = jsonMapper.readTree(text);
                if (rootNode.isObject()) {
                    parseMethodPayload(response, rootNode);
                } else if (rootNode.isArray()) {
                    multiRequest[0] = true;
                    for (int i = 0; i < rootNode.size(); i++) {
                        JsonNode node = rootNode.get(i);
                        parseMethodPayload(response, node);
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
                    response.add(Pair.of(method, payload));
                }
            }
        }
        if (response.size() == 0) {
            // trying to find method from the parameters
            String method = request.getStringProperty("methodName");
            if (method == null) {
                if (methodsByName.size() == 1) {
                    method = defaultMethodInfo.iMethod.getName();
                }
            }
            //
            if (method != null) {
                response.add(Pair.of(method, text == null ? text : text.trim()));
            }
        }
        return response;
    }

    private static void parseMethodPayload(List<Pair<String, String>> response,
            JsonNode node) {
        if (node.isObject()) {
            Iterator<String> it = node.fieldNames();
            if (it.hasNext()) {
                String methodName = it.next();
                String payload = node.get(methodName).toString();
                response.add(Pair.of(methodName, payload));
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
