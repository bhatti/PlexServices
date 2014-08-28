package com.plexobject.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.http.HttpResponse;
import com.plexobject.metrics.Timing;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.ServiceConfig.Method;

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
    private Map<String, Object> headers;
    private String sessionId;
    private Method method;
    private String uri;

    private AbstractResponseDispatcher responseBuilder;

    public RequestBuilder(RequestHandler handler, RoleAuthorizer roleAuthorizer) {
        this.handler = handler;
        this.roleAuthorizer = roleAuthorizer;
    }

    public RequestBuilder setMethod(Method method) {
        this.method = method;
        return this;
    }

    public RequestBuilder setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public RequestBuilder setPayload(String payload) {
        this.payload = payload;
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

    public RequestBuilder setHeaders(Map<String, Object> headers) {
        this.headers = headers;
        return this;
    }

    public RequestBuilder setResponseDispatcher(
            AbstractResponseDispatcher responseBuilder) {
        this.responseBuilder = responseBuilder;
        return this;
    }

    public void invoke() {
        if (handler != null) {
            Timing timing = Timing.begin(handler.getClass());

            ServiceConfig config = handler.getClass().getAnnotation(
                    ServiceConfig.class);
            if (log.isDebugEnabled()) {
                log.debug("Received request for handler "
                        + handler.getClass().getSimpleName() + ", gateway "
                        + config.gateway() + ", payload " + payload
                        + ", params " + params);
            }

            Object object = config.requestClass() != Void.class ? ObjectCodecFactory
                    .getInstance().getObjectCodec(config.codec())
                    .decode(payload, config.requestClass(), params)
                    : null;

            try {
                Request handlerReq = new Request(method, uri, params, headers,
                        object, sessionId, responseBuilder);
                if (config.rolesAllowed() != null
                        && config.rolesAllowed().length > 0
                        && !config.rolesAllowed()[0].equals("")) {
                    roleAuthorizer.authorize(handlerReq, config.rolesAllowed());
                }
                handler.handle(handlerReq);
                timing.endSuccess();
            } catch (AuthException e) {
                responseBuilder.setStatus(HttpResponse.SC_UNAUTHORIZED);
                if (e.getLocation() != null) {
                    responseBuilder.setProperty(Constants.LOCATION,
                            e.getLocation());
                }
                responseBuilder.send(e);
                timing.endSuccess();
            } catch (ValidationException e) {
                responseBuilder.setStatus(HttpResponse.SC_BAD_REQUEST);
                responseBuilder.send(e);
                timing.endSuccess();
            } catch (Exception e) {
                responseBuilder
                        .setStatus(HttpResponse.SC_INTERNAL_SERVER_ERROR);
                responseBuilder.send(e);
                timing.endSuccess();
            }
        } else {
            log.warn("Received Unknown request params " + params + ", payload "
                    + payload);
            responseBuilder.setCodecType(CodecType.HTML);
            responseBuilder.setStatus(HttpResponse.SC_NOT_FOUND);
            responseBuilder.send("page not found");
        }
    }
}
