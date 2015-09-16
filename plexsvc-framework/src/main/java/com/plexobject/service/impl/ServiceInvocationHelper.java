package com.plexobject.service.impl;

import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.plexobject.domain.Constants;
import com.plexobject.domain.Redirectable;
import com.plexobject.domain.Statusable;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.BasePayload;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ws.WSDelegateHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.metrics.ServiceMetrics;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.validation.IRequiredFieldValidator;
import com.plexobject.validation.RequiredFieldValidator;

public class ServiceInvocationHelper {
    private static final String HANDLE = "handle";
    private static final Logger logger = Logger
            .getLogger(ServiceInvocationHelper.class);
    private IRequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator();
    private final ServiceRegistry serviceRegistry;

    public ServiceInvocationHelper(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * This method executes handler by encoding the payload to proper java class
     * and enforces security set by the underlying application.
     * 
     * @param request
     * @param handler
     */
    public void invoke(Request request, RequestHandler handler,
            final ServiceRegistry registry) {
        if (handler != null) {
            final long started = System.currentTimeMillis();
            ServiceMetrics metrics = serviceRegistry
                    .getServiceMetricsRegistry().getServiceMetrics(handler);

            ServiceConfigDesc config = serviceRegistry
                    .getServiceConfig(handler);
            if (logger.isDebugEnabled()) {
                logger.debug("Received request for handler "
                        + handler.getClass().getSimpleName() + ", protocol "
                        + config.protocol() + ", request " + request);
            }

            // check if payload is required
            if (config.contentsClass() != null
                    && config.contentsClass() != Void.class
                    && request.getContents() == null
                    && request.getProperties().size() == 0) {
                request.getResponse().setStatus(HttpResponse.SC_FORBIDDEN);
                request.getResponse().setContents(
                        "Expected payload of type "
                                + config.contentsClass().getName()
                                + ", but payload was: " + request);
                request.sendResponse();
                return;
            }
            ((AbstractResponseDispatcher) request.getResponseDispatcher())
                    .setOutgoingInterceptorsLifecycle(serviceRegistry);
            CodecType codecType = CodecType.fromAcceptHeader(
                    (String) request.getStringProperty(Constants.ACCEPT),
                    config.codec());
            request.getResponse().setCodecType(codecType);

            // We assume incoming payload is text so we will run through input
            // interceptors
            runInputInterceptors(request);
            //
            if (codecType != CodecType.SERVICE_SPECIFIC
                    && request.getContents() instanceof String) {
                deserializePayload(request, config, codecType);
            }
            //
            // Invoking request interceptors
            if (registry.hasRequestInterceptors()) {
                for (Interceptor<Request> interceptor : registry
                        .getRequestInterceptors()) {
                    request = interceptor.intercept(request);
                }
            }
            // Note: output interceptors are executed from
            // AbstractResponseDispatcher

            try {
                // update post parameters
                if (request.isFormRequest()) {
                    addFormPropertiesFromPayload(request);
                }
                //
                // validate required fields
                requiredFieldValidator.validate(handler, request.getContents(),
                        request.getProperties());

                //
                //
                invokeWithAroundInterceptorIfNeeded(request, handler, registry,
                        started, metrics, config);
            } catch (Exception e) {
                metrics.incrementErrors();
                if (e instanceof Redirectable) {
                    if (((Redirectable) e).getLocation() != null) {
                        request.getResponse().setLocation(
                                ((Redirectable) e).getLocation());
                    }
                } else if (e instanceof FileNotFoundException) {
                    request.getResponse().setStatus(HttpResponse.SC_NOT_FOUND);
                } else if (e instanceof Statusable) {
                    request.getResponse().setStatus(
                            ((Statusable) e).getStatus());
                } else {
                    request.getResponse().setStatus(
                            HttpResponse.SC_INTERNAL_SERVER_ERROR);
                }
                request.getResponse().setContents(e);
                request.sendResponse();
            }
        } else {
            logger.warn("PLEXSVC Received Unknown request params "
                    + request.getProperties() + ", payload "
                    + request.getContents());
            request.getResponse().setCodecType(CodecType.TEXT);
            request.getResponse().setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponse().setContents("page not found");
            request.sendResponse();
        }
    }

    private static void addFormPropertiesFromPayload(Request request) {
        String[] nvArr = request.getContents().toString().split("&");
        for (String nvStr : nvArr) {
            String[] nv = nvStr.split("=");
            if (nv.length == 2) {
                request.setProperty(nv[0], nv[1]);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void runInputInterceptors(Request request) {
        // apply input interceptors
        if (serviceRegistry.hasInputInterceptors()) {
            BasePayload payload = request;
            for (Interceptor<BasePayload<String>> interceptor : serviceRegistry
                    .getInputInterceptors()) {
                payload = interceptor.intercept(payload);
            }
        }
    }

    private void deserializePayload(Request request, ServiceConfigDesc config,
            CodecType codecType) {
        String textPayload = request.getContentsAs();
        // decode text input into object
        if (config.contentsClass() != null
                && config.contentsClass() != Void.class
                && (request.getContents() instanceof String || request
                        .getContents() == null)) {
            request.setContents(ObjectCodecFactory
                    .getInstance()
                    .getObjectCodec(codecType)
                    .decode(textPayload, config.contentsClass(),
                            request.getProperties()));
        }
    }

    private void invokeWithAroundInterceptorIfNeeded(final Request request,
            final RequestHandler handler, final ServiceRegistry registry,
            final long started, final ServiceMetrics metrics,
            final ServiceConfigDesc config) throws Exception {
        if (handler instanceof WSDelegateHandler) {
            // skip authorization and around interceptor for JavaWS because we
            // need additional info for method names
            invoke(request, handler, started, metrics, config);

        } else {
            //
            if (serviceRegistry.getSecurityAuthorizer() != null) {
                serviceRegistry.getSecurityAuthorizer().authorize(request,
                        config.rolesAllowed());
            }

            if (registry.getAroundInterceptor() != null) {
                Callable<Object> callable = new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        invoke(request, handler, started, metrics, config);
                        return null;
                    }
                };
                registry.getAroundInterceptor().proceed(handler, HANDLE,
                        callable);
            } else {
                invoke(request, handler, started, metrics, config);
            }
        }
    }

    private static void invoke(Request request, RequestHandler handler,
            final long started, ServiceMetrics metrics, ServiceConfigDesc config) {
        // invoke authorizer if set
        handler.handle(request);
        metrics.addResponseTime(System.currentTimeMillis() - started);
        // send back the reply
        if (request.getResponse().getContents() != null) {
            request.sendResponse();
        }
    }

}
