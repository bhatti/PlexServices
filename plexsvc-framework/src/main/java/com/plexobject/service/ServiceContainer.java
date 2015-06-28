package com.plexobject.service;

import java.util.Collection;

import com.plexobject.handler.RequestHandler;

/**
 * This interface defines service containers that executes handlers
 * 
 * @author shahzad bhatti
 *
 */
public interface ServiceContainer extends Lifecycle {
    void addRequestHandler(RequestHandler handler);

    boolean removeRequestHandler(RequestHandler handler);

    boolean existsRequestHandler(RequestHandler handler);

    Collection<RequestHandler> getHandlers();
}
