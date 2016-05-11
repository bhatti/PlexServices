package com.plexobject.handler.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.http.ServiceInvocationException;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;
import com.plexobject.util.ReflectUtils.ParamType;
import com.plexobject.util.SameThreadExecutorService;

public class WSDelegateHandler implements RequestHandler {
    private static final String SERIAL_EXECUTION = "serialExecution";
    private static final Logger logger = Logger
            .getLogger(WSDelegateHandler.class);
    //
    private static final String RESPONSE_SUFFIX = "Response";
    //
    private final Object delegate;
    private final ServiceRegistry serviceRegistry;
    private final Map<String, WSServiceMethod> methodsByName = new HashMap<>();
    private WSServiceMethod defaultMethodInfo;
    private ExecutorService sameThreadExecutorService = new SameThreadExecutorService();

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
    public void handle(final Request incomingRequest) {
        MethodPayLoadRequest methodPayLoadRequest = MethodPayLoadRequest
                .getMethodNameAndPayloads(
                        incomingRequest,
                        methodsByName.size() == 1 ? defaultMethodInfo.iMethod
                                .getName() : null);
        if (methodPayLoadRequest.multiRequest) {
            handleMultiRequests(incomingRequest, methodPayLoadRequest);
        } else {
            invokeRequest(methodPayLoadRequest.requests.get(0),
                    incomingRequest, methodPayLoadRequest.multiRequest);
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    //
    private ExecutorService getExecutorService(final Request request) {
        if (request.getBooleanProperty(SERIAL_EXECUTION, false)) {
            return sameThreadExecutorService;
        } else {
            return serviceRegistry.getDefaultExecutorService();
        }
    }

    private void handleMultiRequests(final Request incomingRequest,
            final MethodPayLoadRequest methodPayLoadRequest) {
        Map<Future<Request>, MethodPayLoadInfo> futuresAndMethodPayLoadInfo = new LinkedHashMap<>();
        for (int n = 0; n < methodPayLoadRequest.requests.size(); n++) {
            final Request request = new Request(incomingRequest);
            final MethodPayLoadInfo methodPayLoadInfo = methodPayLoadRequest.requests
                    .get(n);
            Future<Request> future = getExecutorService(incomingRequest)
                    .submit(new Callable<Request>() {
                        @Override
                        public Request call() throws Exception {
                            invokeRequest(methodPayLoadInfo, request, true);
                            return request;
                        }
                    });
            futuresAndMethodPayLoadInfo.put(future, methodPayLoadInfo);
        }
        //
        final List<Object> allResponses = new ArrayList<>();
        for (Future<Request> future : futuresAndMethodPayLoadInfo.keySet()) {
            try {
                Request request = future.get();
                allResponses.add(request.getResponse().getContents());
                incomingRequest.getResponse().getHeaders()
                        .putAll(request.getResponse().getHeaders());
                incomingRequest.getResponse().getProperties()
                        .putAll(request.getResponse().getProperties());
            } catch (Exception e) {
                final MethodPayLoadInfo methodPayLoadInfo = futuresAndMethodPayLoadInfo
                        .get(future);
                Map<String, Object> response = buildExceptionResponse(e,
                        methodPayLoadInfo);
                allResponses.add(response);
            }
        }
        incomingRequest.getResponse().setContents(allResponses);
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
                request.getResponse()
                        .setContents(
                                buildExceptionResponse((Exception) request
                                        .getResponse().getContents(),
                                        methodPayLoadInfo));
            }
        }
    }

    private Map<String, Object> buildExceptionResponse(Exception e,
            final MethodPayLoadInfo methodPayLoadInfo) {
        final String responseTag = methodPayLoadInfo.method + RESPONSE_SUFFIX;
        Map<String, Object> response = new HashMap<String, Object>();
        response.put(responseTag, e);
        return response;
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

    private Request requestAfterRequestInterceptors(Request request) {
        if (serviceRegistry.hasRequestInterceptors()) {
            for (Interceptor<Request> interceptor : serviceRegistry
                    .getRequestInterceptors()) {
                request = interceptor.intercept(request);
            }
        }
        return request;
    }

    @Override
    public String toString() {
        return "WSDelegateHandler [delegate=" + delegate + "]";
    }
}
