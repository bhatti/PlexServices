package com.plexobject.service;

import java.util.Collection;

import com.plexobject.handler.RequestHandler;

public interface ServiceGateway extends Lifecycle {
    void add(RequestHandler handler);

    void remove(RequestHandler handler);
    
    Collection<RequestHandler> getHandlers();
}
