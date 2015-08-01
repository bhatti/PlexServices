package com.plexobject.bus.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.service.impl.AbstractServiceContainer;

public class EventBusServiceContainer extends AbstractServiceContainer {
    private final Map<RequestHandler, Long> handlers = new ConcurrentHashMap<>();

    public EventBusServiceContainer(final ServiceRegistry serviceRegistry)
            throws Exception {
        super(serviceRegistry);
    }

    public void init() {
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }

    @Override
    public Collection<RequestHandler> getHandlers() {
        return new ArrayList<RequestHandler>(handlers.keySet());
    }

    @Override
    public synchronized void addRequestHandler(final RequestHandler handler) {
        ServiceConfigDesc config = serviceRegistry.getServiceConfig(handler);
        if (config == null) {
            throw new IllegalArgumentException("service handler " + handler
                    + " doesn't define ServiceConfig annotation");
        }

        String destName = config.endpoint();

        if (handlers.containsKey(handler)) {
            throw new IllegalStateException(
                    "RequestHandler is already registered for " + destName);
        }
        Long subscriberId = serviceRegistry.getEventBus().subscribe(
                config.endpoint(), new RequestHandler() {
                    @Override
                    public void handle(Request request) {
                        serviceRegistry.invoke(request, handler);
                    }
                }, null);
        handlers.put(handler, subscriberId);
        logger.info("PLEXSVC Adding EventBus service " + handler + " for "
                + handler + ", codec " + config.codec());
    }

    @Override
    public synchronized boolean removeRequestHandler(RequestHandler h) {
        logger.info("PLEXSVC Removing EventBus service " + h);
        Long subscriberId = handlers.remove(h);
        if (subscriberId != null) {
            serviceRegistry.getEventBus().unsubscribe(subscriberId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean existsRequestHandler(RequestHandler handler) {
        return handlers.containsKey(handler);
    }
}
