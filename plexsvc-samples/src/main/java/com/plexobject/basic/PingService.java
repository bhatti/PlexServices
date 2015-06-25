package com.plexobject.basic;

import org.apache.log4j.Logger;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/ping", method = RequestMethod.GET, codec = CodecType.JSON, concurrency = 10)
public class PingService implements RequestHandler {
    private static final Logger log = Logger.getLogger(PingService.class);

    public PingService() {
        log.info("Ping Service Started");
    }

    @Override
    public void handle(Request<Object> request) {
        String data = request.getStringProperty("data");
        if (data == null) {
            data = "";
        }
        request.getResponse().setPayload(data);
    }
}
