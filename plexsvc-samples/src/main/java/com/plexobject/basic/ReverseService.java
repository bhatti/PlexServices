package com.plexobject.basic;

import org.apache.log4j.Logger;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/reverse", method = RequestMethod.POST, codec = CodecType.JSON)
public class ReverseService implements RequestHandler {
    private static final Logger log = Logger.getLogger(ReverseService.class);

    public ReverseService() {
        log.info("Reverse Service Started");
    }

    @Override
    public void handle(Request request) {
        String data = request.getStringProperty("data");
        if (data == null) {
            data = "";
        }
        log.info("Received " + data);
        request.getResponse().setPayload(
                new StringBuilder(data).reverse().toString());
    }
}
