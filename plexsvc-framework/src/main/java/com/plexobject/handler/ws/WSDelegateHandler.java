package com.plexobject.handler.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

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
    //
    private static final String RESPONSE_SUFFIX = "Response";
    //
    private final Object delegate;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, WSServiceMethod> methodsByName = new HashMap<>();
    private WSServiceMethod defaultMethodInfo;

    //
    public WSDelegateHandler(Object delegate, ServiceRegistry registry) {
        this.delegate = delegate;
        this.serviceRegistry = registry;
    }

    public void addMethod(WSServiceMethod info) {
        methodsByName.put(info.iMethod.getName(), info);
        defaultMethodInfo = info;
    }

    @Override
    public void handle(final Request rawRequest) {
        MethodPayLoadRequest methodPayLoadRequest = MethodPayLoadRequest
                .getMethodNameAndPayloads(
                        rawRequest,
                        methodsByName.size() == 1 ? defaultMethodInfo.iMethod
                                .getName() : null);
        final Request[] allRequests = new Request[methodPayLoadRequest.requests
                .size()];
        for (int n = 0; n < methodPayLoadRequest.requests.size(); n++) {
            if (n == 0) {
                allRequests[n] = rawRequest;
            } else {
                allRequests[n] = new Request(rawRequest);
            }
        }
        //
        for (int n = 0; n < methodPayLoadRequest.requests.size(); n++) {
            invokeRequest(methodPayLoadRequest.requests.get(n), allRequests[n],
                    methodPayLoadRequest.multiRequest);
        }
        //
        if (methodPayLoadRequest.multiRequest) {
            List<Object> allResponses = new ArrayList<>();
            for (Request req : allRequests) {
                allResponses.add(req.getResponse().getContents());
            }
            rawRequest.getResponse().setContents(allResponses);
        }
    }

    private void invokeRequest(MethodPayLoadInfo methodPayLoadInfo,
            final Request request, boolean multiRequest) {
        final String responseTag = methodPayLoadInfo.method + RESPONSE_SUFFIX;
        //
        final WSServiceMethod methodInfo = methodsByName
                .get(methodPayLoadInfo.method);
        try {
            if (methodInfo == null) {
                logger.warn("PLEXSVC: Unknown method '"
                        + methodPayLoadInfo.method + "', available "
                        + methodsByName.keySet() + ", request "
                        + request.getContents());
                throw new ServiceInvocationException("Unknown method '"
                        + methodPayLoadInfo.method + "'",
                        multiRequest ? HttpResponse.SC_OK
                                : HttpResponse.SC_NOT_FOUND);
            }
            // set method name
            request.setMethodName(methodInfo.iMethod.getName());
            //
            // make sure you use iMethod to decode because implMethod might
            // have erased parameterized type
            // We can get input parameters either from JSON text, form/query
            // parameters or method simply takes Map so we just pass all
            // request properties
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
                    methodPayLoadInfo.payload, request.getCodec());
            if (args.length > 0) {
                request.setContents(args[0]); // Generally, first
                // argument will be the decoded object
            }
            invokeWithAroundInterceptorIfNeeded(request, methodInfo,
                    responseTag, args);
        } catch (Exception e) {
            logger.error("PLEXSVC Failed to invoke "
                    + (methodInfo != null ? methodInfo.iMethod
                            : " unknown-method") + ", for request " + request,
                    e);
            request.getResponse().setContents(e);
        }
        if (multiRequest) {
            if (request.getResponse().getContents() instanceof Exception) {
                Map<String, Object> response = new HashMap<String, Object>();
                response.put(responseTag, request.getResponse().getContents());
                request.getResponse().setContents(response);
            }
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
}
