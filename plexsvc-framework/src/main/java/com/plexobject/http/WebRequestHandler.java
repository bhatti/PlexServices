package com.plexobject.http;

import java.util.Map;

import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.service.ServiceConfig.Method;

public interface WebRequestHandler {
    void handle(Method method, String uri, String payload,
            Map<String, Object> params, Map<String, Object> headers,
            AbstractResponseDispatcher dispatcher);
}
