package com.plexobject.service;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;

public class RequestBuilder {
    private static final Logger log = LoggerFactory
            .getLogger(RequestBuilder.class);
    private static final Set<String> FORBIDDEN_EXCEPTION_FIELDS = new HashSet<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("class");
            add("stackTrace");
            add("cause");
            add("localizedMessage");
            add("suppressed");
        }
    };
    private final RequestHandler handler;
    private final RoleAuthorizer roleAuthorizer;
    private String payload;
    private Map<String, Object> params;
    private String remoteAddress;
    private String sessionId;
    private ResponseBuilder responseBuilder;

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

    public RequestBuilder setResponseBuilder(ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
        return this;
    }

    public void invoke() {
        if (handler != null) {
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
                    log.info("Authorizing request " + handlerReq + " for "
                            + config.rolesAllowed()[0]);
                    roleAuthorizer.authorize(handlerReq, config.rolesAllowed());
                }
                handler.handle(handlerReq);
            } catch (AuthException e) {
                responseBuilder.setStatus(Constants.SC_UNAUTHORIZED);
                if (e.getLocation() != null) {
                    responseBuilder.setProperty(Constants.LOCATION,
                            e.getLocation());
                }
                responseBuilder.send(toMap(e));
            } catch (ValidationException e) {
                responseBuilder.setStatus(Constants.SC_BAD_REQUEST);
                responseBuilder.send(toMap(e));

            } catch (Exception e) {
                responseBuilder.setStatus(Constants.SC_INTERNAL_SERVER_ERROR);
                responseBuilder.send(toMap(e));
            }
        } else {
            log.warn("Received Unknown request params " + params + ", payload "
                    + payload);
            responseBuilder.setContentType("text/html;charset=utf-8");
            responseBuilder.setStatus(Constants.SC_NOT_FOUND);
            responseBuilder.send("page not found");
        }
    }

    private static Map<String, Object> toMap(Exception e) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            BeanInfo info = Introspector.getBeanInfo(e.getClass());
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader != null
                        && !FORBIDDEN_EXCEPTION_FIELDS.contains(pd.getName())) {
                    map.put(pd.getName(), reader.invoke(e));
                }
            }
        } catch (Exception ex) {
            log.error("Failed to convert exception " + e, ex);
        }
        return map;
    }
}
