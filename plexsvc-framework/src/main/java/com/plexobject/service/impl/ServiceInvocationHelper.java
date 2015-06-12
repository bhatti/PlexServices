package com.plexobject.service.impl;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import com.plexobject.domain.Constants;
import com.plexobject.domain.Redirectable;
import com.plexobject.domain.Statusable;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.metrics.ServiceMetrics;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.IncomingInterceptorsLifecycle;
import com.plexobject.service.Interceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.validation.IRequiredFieldValidator;
import com.plexobject.validation.RequiredFieldValidator;

public class ServiceInvocationHelper {
    private static final Logger log = Logger
            .getLogger(ServiceInvocationHelper.class);
    private IRequiredFieldValidator requiredFieldValidator = new RequiredFieldValidator();
    private final ServiceRegistry serviceRegistry;
    private final RoleAuthorizer authorizer;

    public ServiceInvocationHelper(ServiceRegistry serviceRegistry,
            RoleAuthorizer authorizer) {
        this.serviceRegistry = serviceRegistry;
        this.authorizer = authorizer;
    }

    /**
     * This method executes handler by encoding the payload to proper java class
     * and enforces security set by the underlying application.
     * 
     * @param request
     * @param handler
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    // igoring type of Request so that we can cast to Object
    public void invoke(Request request, RequestHandler handler,
            IncomingInterceptorsLifecycle incomingInterceptorsLifecycle) {
        if (handler != null) {
            final long started = System.currentTimeMillis();
            ServiceMetrics metrics = serviceRegistry
                    .getServiceMetricsRegistry().getServiceMetrics(handler);

            ServiceConfigDesc config = serviceRegistry
                    .getServiceConfig(handler);
            if (log.isDebugEnabled()) {
                log.debug("Received request for handler "
                        + handler.getClass().getSimpleName() + ", protocol "
                        + config.protocol() + ", request " + request);
            }

            // check if payload is required
            if (config.payloadClass() != null
                    && config.payloadClass() != Void.class
                    && request.getPayload() == null
                    && request.getProperties().size() == 0) {
                request.getResponse().setStatus(HttpResponse.SC_FORBIDDEN);
                request.getResponse().setPayload(
                        "Expected payload of type "
                                + config.payloadClass().getName()
                                + ", but payload was: " + request);
                request.sendResponse();
                return;
            }
            ((AbstractResponseDispatcher) request.getResponseDispatcher())
                    .setOutgoingInterceptorsLifecycle(serviceRegistry);
            CodecType codecType = CodecType.fromAcceptHeader(
                    (String) request.getProperty(Constants.ACCEPT),
                    config.codec());
            request.getResponse().setCodecType(codecType);

            // We assume incoming payload is text so we will run through input
            // interceptors
            String textPayload = request.getPayload();
            if (incomingInterceptorsLifecycle != null) {
                // apply input interceptors
                if (incomingInterceptorsLifecycle.hasInputInterceptors()) {
                    for (Interceptor<String> interceptor : incomingInterceptorsLifecycle
                            .getInputInterceptors()) {
                        textPayload = interceptor.intercept(textPayload);
                    }
                    request.setPayload(textPayload);
                }
            }

            // decode text input into object
            if (config.payloadClass() != null
                    && config.payloadClass() != Void.class
                    && (request.getPayload() instanceof String || request
                            .getPayload() == null)) {
                request.setPayload(ObjectCodecFactory
                        .getInstance()
                        .getObjectCodec(codecType)
                        .decode(textPayload, config.payloadClass(),
                                request.getProperties()));
            }

            // Invoking request interceptors
            if (incomingInterceptorsLifecycle != null
                    && incomingInterceptorsLifecycle.hasRequestInterceptors()) {
                for (Interceptor<Request<Object>> interceptor : incomingInterceptorsLifecycle
                        .getRequestInterceptors()) {
                    request = interceptor.intercept(request);
                }
            }
            // Note: output interceptors are executed from
            // AbstractResponseDispatcher

            // validate required fields
            requiredFieldValidator.validate(handler,
                    request.getPayload() == null ? request.getProperties()
                            : request.getPayload());

            // update post parameters
            if (request.getPayload() != null
                    && request.getProperties().size() == 0
                    && request.getProperties().size() % 2 == 0) {
                String[] nvArr = request.getPayload().toString().split("&");
                for (String nvStr : nvArr) {
                    String[] nv = nvStr.split("=");
                    if (nv.length == 2) {
                        request.setProperty(nv[0], nv[1]);
                    }
                }
            }
            //
            try {
                //
                // invoke authorizer if set
                authorizeIfNeeded(request, config);
                handler.handle(request);
                metrics.addResponseTime(System.currentTimeMillis() - started);
                // send back the reply
                if (request.getResponse().getPayload() != null) {
                    request.sendResponse();
                }
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
                request.getResponse().setPayload(e);
                request.sendResponse();
            }
        } else {
            log.warn("Received Unknown request params "
                    + request.getProperties() + ", payload "
                    + request.getPayload());
            request.getResponse().setCodecType(CodecType.TEXT);
            request.getResponse().setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponse().setPayload("page not found");
            request.sendResponse();
        }
    }

    private void authorizeIfNeeded(Request<Object> request,
            ServiceConfigDesc config) {
        if (authorizer != null && config.rolesAllowed() != null
                && config.rolesAllowed().length > 0
                && !config.rolesAllowed()[0].equals("")) {
            authorizer.authorize(request, config.rolesAllowed());
        }
    }

}
