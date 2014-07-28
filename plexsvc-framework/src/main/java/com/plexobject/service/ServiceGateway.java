package com.plexobject.service;

import com.plexobject.handler.RequestHandler;

public interface ServiceGateway extends Lifecycle {
    void add(RequestHandler service);

    void remove(RequestHandler service);
}
