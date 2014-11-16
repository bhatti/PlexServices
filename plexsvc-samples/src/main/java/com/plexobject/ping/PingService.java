package com.plexobject.ping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfig.Protocol;

@ServiceConfig(protocol = Protocol.HTTP, requestClass = Void.class, endpoint = "/ping", method = Method.GET, codec = CodecType.JSON)
public class PingService implements RequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(PingService.class);

    @Override
    public void handle(Request request) {
        String data = request.getProperty("data");
        if (data == null) {
            data = "";
        }
        log.info("Received " + request.getProtocol());
        request.getResponseDispatcher().send(data);
    }
}
