package com.plexobject.service.impl;

import java.io.FileNotFoundException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Redirectable;
import com.plexobject.domain.Statusable;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.metrics.ServiceMetrics;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.RequestInterceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.validation.IRequiredFieldValidator;
import com.plexobject.validation.RequiredFieldValidator;

public class ServiceInvocationHelper {
    private static final Logger log = LoggerFactory
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
    public void invoke(Request request, RequestHandler handler,
            Collection<RequestInterceptor> interceptors) {
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

            // override payload in request
            Object payload = null;
            if (config.payloadClass() != Void.class) {
                payload = ObjectCodecFactory
                        .getInstance()
                        .getObjectCodec(config.codec())
                        .decode((String) request.getPayload(),
                                config.payloadClass(), request.getProperties());
                if (payload == null) {
                    request.getResponseDispatcher().setStatus(
                            HttpResponse.SC_FORBIDDEN);
                    request.getResponseDispatcher().send(
                            "Expected payload not defined");
                }
            }

            // validate required fields
            requiredFieldValidator.validate(handler,
                    payload == null ? request.getProperties() : payload);

            // update post parameters
            if (payload != null) {
                request.setPayload(payload);
            } else if (request.getPayload() != null) {
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
                // invoke interceptor if needed
                request = invokeInterceptorIfneeded(request, handler,
                        interceptors);
                //
                // invoke authorizer if set
                authorizeIfNeeded(request, config);
                handler.handle(request);
                metrics.addResponseTime(System.currentTimeMillis() - started);
            } catch (Exception e) {
                metrics.incrementErrors();
                if (e instanceof Redirectable) {
                    if (((Redirectable) e).getLocation() != null) {
                        request.getResponseDispatcher().setLocation(
                                ((Redirectable) e).getLocation());
                    }
                } else if (e instanceof FileNotFoundException) {
                    request.getResponseDispatcher().setStatus(
                            HttpResponse.SC_NOT_FOUND);
                } else if (e instanceof Statusable) {
                    request.getResponseDispatcher().setStatus(
                            ((Statusable) e).getStatus());
                } else {
                    request.getResponseDispatcher().setStatus(
                            HttpResponse.SC_INTERNAL_SERVER_ERROR);
                }
                request.getResponseDispatcher().send(e);
            }
        } else {
            log.warn("Received Unknown request params "
                    + request.getProperties() + ", payload "
                    + request.getPayload());
            request.getResponseDispatcher().setCodecType(CodecType.TEXT);
            request.getResponseDispatcher()
                    .setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponseDispatcher().send("page not found");
        }
    }

    private Request invokeInterceptorIfneeded(Request request,
            RequestHandler handler, Collection<RequestInterceptor> interceptors) {
        if (interceptors != null) {
            for (RequestInterceptor interceptor : interceptors) {
                request = interceptor.intercept(request);
            }
        }
        return request;
    }

    private void authorizeIfNeeded(Request request, ServiceConfigDesc config) {
        if (authorizer != null && config.rolesAllowed() != null
                && config.rolesAllowed().length > 0
                && !config.rolesAllowed()[0].equals("")) {
            authorizer.authorize(request, config.rolesAllowed());
        }
    }

}
