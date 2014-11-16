package com.plexobject.service;

import com.plexobject.handler.RequestHandler;

public class ServiceHandlerLifecycle implements ServiceHandlerLifecycleMBean {
    private final ServiceRegistry registry;
    private final RequestHandler handler;

    public ServiceHandlerLifecycle(final ServiceRegistry registry,
            final RequestHandler handler) {
        this.registry = registry;
        this.handler = handler;
    }

    @Override
    public void start() {
        registry.add(handler);
    }

    @Override
    public void stop() {
        registry.remove(handler);
    }

    @Override
    public boolean isRunning() {
        return registry.exists(handler);
    }

    @Override
    public int ping() {
        if (handler instanceof Pingable) {
            return ((Pingable) handler).ping();
        }
        return -1;
    }

    @Override
    public String getSummary() {
        return registry.getServiceMetricsRegistry().getServiceMetrics(handler)
                .getSummary();
    }
}
