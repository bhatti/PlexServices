package com.plexobject.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.metrics.Timing;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;

/**
 * This class builds remote request
 * 
 * @author shahzad bhatti
 *
 */
public class RequestBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(RequestBuilder.class);
    private final RequestHandler handler;
    private final RoleAuthorizer roleAuthorizer;
    private String payload;
    private Map<String, Object> params;
    private String remoteAddress;
    private String sessionId;
    private AbstractResponseBuilder responseBuilder;

    public RequestBuilder(RequestHandler handler, RoleAuthorizer roleAuthorizer) {
        this.handler = handler;
        this.roleAuthorizer = roleAuthorizer;
    }

    public RequestBuilder setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public RequestBuilder setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public RequestBuilder setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public RequestBuilder setParameters(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public RequestBuilder setResponseBuilder(
            AbstractResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
        return this;
    }

    public void invoke() {
        if (handler != null) {
            Timing timing = Timing.begin(handler.getClass().getSimpleName());

            ServiceConfig config = handler.getClass().getAnnotation(
                    ServiceConfig.class);
            if (log.isDebugEnabled()) {
                log.debug("Received request for handler "
                        + handler.getClass().getSimpleName() + ", gateway "
                        + config.gateway() + ", payload " + payload
                        + ", params " + params);
            }

            Object object = config.requestClass() != Void.class ? ObjectCodeFactory
                    .getObjectCodec(config.codec()).decode(payload,
                            config.requestClass(), params) : null;
            try {
                Request handlerReq = new Request(params, object, sessionId,
                        remoteAddress, responseBuilder);
                if (config.rolesAllowed() != null
                        && config.rolesAllowed().length > 0
                        && !config.rolesAllowed()[0].equals("")) {
                    roleAuthorizer.authorize(handlerReq, config.rolesAllowed());
                }
                handler.handle(handlerReq);
                timing.endSuccess();
            } catch (AuthException e) {
                responseBuilder.setStatus(Constants.SC_UNAUTHORIZED);
                if (e.getLocation() != null) {
                    responseBuilder.setProperty(Constants.LOCATION,
                            e.getLocation());
                }
                responseBuilder.send(e);
                timing.endSuccess();
            } catch (ValidationException e) {
                responseBuilder.setStatus(Constants.SC_BAD_REQUEST);
                responseBuilder.send(e);
                timing.endSuccess();
            } catch (Exception e) {
                responseBuilder.setStatus(Constants.SC_INTERNAL_SERVER_ERROR);
                responseBuilder.send(e);
                timing.endSuccess();
            }
        } else {
            log.warn("Received Unknown request params " + params + ", payload "
                    + payload);
            responseBuilder.setContentType("text/html;charset=utf-8");
            responseBuilder.setStatus(Constants.SC_NOT_FOUND);
            responseBuilder.send("page not found");
        }
    }
}
