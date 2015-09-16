package com.plexobject.basic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/array", method = RequestMethod.GET, codec = CodecType.JSON)
public class ArrayService implements RequestHandler {
    private static final Logger log = Logger.getLogger(ReverseService.class);

    public ArrayService() {
        log.info("Array Service Started");
    }

    @Override
    public void handle(Request request) {
        Integer count = request.getIntegerProperty("count");
        if (count == null) {
            count = 1;
        }
        List<Map<String, Object>> response = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", new Date());
            entry.put("id", i + 1);
            response.add(entry);
        }
        request.getResponse().setContents(response);
    }
}
