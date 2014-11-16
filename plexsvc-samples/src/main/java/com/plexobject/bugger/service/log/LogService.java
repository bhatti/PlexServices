package com.plexobject.bugger.service.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfig.Method;

// @ServiceConfig(protocol = Protocol.HTTP, requestClass = Void.class,
// endpoint = "/logs", method = Method.POST,
// contentType = "application/json")
@ServiceConfig(protocol = Protocol.JMS, requestClass = Void.class, endpoint = "queue:{scope}-log-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
public class LogService implements RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(LogService.class);

    @Override
    public void handle(Request request) {
        log.info(request.toString());
    }

}