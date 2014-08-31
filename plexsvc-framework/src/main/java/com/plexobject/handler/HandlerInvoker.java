package com.plexobject.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.http.HttpResponse;
import com.plexobject.metrics.Timing;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig;

/**
 * This class executes handler by encoding the payload to proper java class and
 * enforces security set by the underlying application.
 * 
 * @author shahzad bhatti
 *
 */
public class HandlerInvoker {
    private static final Logger log = LoggerFactory
            .getLogger(HandlerInvoker.class);

    public static void invoke(Request request, RequestHandler handler,
            RoleAuthorizer roleAuthorizer) {
        if (handler != null) {
            Timing timing = Timing.begin(handler.getClass());

            ServiceConfig config = handler.getClass().getAnnotation(
                    ServiceConfig.class);
            if (log.isDebugEnabled()) {
                log.debug("Received request for handler "
                        + handler.getClass().getSimpleName() + ", gateway "
                        + config.gateway() + ", payload "
                        + request.getPayload() + ", params "
                        + request.getProperties());
            }

            // override payload in request
            Object payload = config.requestClass() != Void.class ? ObjectCodecFactory
                    .getInstance()
                    .getObjectCodec(config.codec())
                    .decode(request.getPayload(), config.requestClass(),
                            request.getProperties())
                    : null;
            request.setPayload(payload);
            try {
                if (roleAuthorizer != null && config.rolesAllowed() != null
                        && config.rolesAllowed().length > 0
                        && !config.rolesAllowed()[0].equals("")) {
                    roleAuthorizer.authorize(request, config.rolesAllowed());
                }
                handler.handle(request);
                timing.endSuccess();
            } catch (AuthException e) {
                request.getResponseDispatcher().setStatus(
                        HttpResponse.SC_UNAUTHORIZED);
                if (e.getLocation() != null) {
                    request.getResponseDispatcher().setProperty(
                            Constants.LOCATION, e.getLocation());
                }
                request.getResponseDispatcher().send(e);
                timing.endSuccess();
            } catch (ValidationException e) {
                request.getResponseDispatcher().setStatus(
                        HttpResponse.SC_BAD_REQUEST);
                request.getResponseDispatcher().send(e);
                timing.endSuccess();
            } catch (Exception e) {
                request.getResponseDispatcher().setStatus(
                        HttpResponse.SC_INTERNAL_SERVER_ERROR);
                request.getResponseDispatcher().send(e);
                timing.endSuccess();
            }
        } else {
            log.warn("Received Unknown request params "
                    + request.getProperties() + ", payload "
                    + request.getPayload());
            request.getResponseDispatcher().setCodecType(CodecType.HTML);
            request.getResponseDispatcher()
                    .setStatus(HttpResponse.SC_NOT_FOUND);
            request.getResponseDispatcher().send("page not found");
        }
    }

}
