package com.plexobject.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.ObjectCodeFactory;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ResponseBuilder;

public class RequestHandlerUtils {
    private static final Logger log = LoggerFactory
            .getLogger(RequestHandlerUtils.class);

    public static final boolean invokeHandler(RequestHandler handler,
            String text, Map<String, Object> params,
            ResponseBuilder responseBuilder) {
        if (handler != null) {
            ServiceConfig config = handler.getClass().getAnnotation(
                    ServiceConfig.class);
            if (log.isDebugEnabled()) {
                log.debug("Received request for handler "
                        + handler.getClass().getSimpleName() + ", gateway "
                        + config.gateway() + ", text " + text + ", params "
                        + params);
            }
            Object object = config.requestClass() != Void.class ? ObjectCodeFactory
                    .getObjectCodec(config.codec()).decode(text,
                            config.requestClass(), params) : null;
            try {
                Request handlerReq = new Request(params, object,
                        responseBuilder);
                handler.handle(handlerReq);
            } catch (Exception e) {
                responseBuilder.sendError(e);
            }
            return true;
        } else {
            log.info("Received Unknown request params " + params + ", text "
                    + text);
            return false;
        }
    }

}
