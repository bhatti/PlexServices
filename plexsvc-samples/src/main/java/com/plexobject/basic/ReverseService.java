package com.plexobject.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/reverse", method = Method.POST, codec = CodecType.JSON)
public class ReverseService implements RequestHandler {
    private static final Logger log = LoggerFactory
            .getLogger(ReverseService.class);

    public ReverseService() {
        log.info("Reverse Service Started");
    }

    @Override
    public void handle(Request request) {
        String data = request.getProperty("data");
        if (data == null) {
            data = "";
        }
        log.info("Received " + data);
        request.getResponseDispatcher().send(
                new StringBuilder(data).reverse().toString());
    }
}
