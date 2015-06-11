package com.plexobject.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.domain.Preconditions;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfigDesc;

/**
 * This is a helper class to manage request handlers and interceptors
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistryHandlers {
    private final ConcurrentHashMap<RequestHandler, ServiceConfigDesc> serviceConfigsByHandler = new ConcurrentHashMap<>();

    /**
     * This method returns cached ServiceConfigDesc for given handler
     * 
     * @param h
     * @return
     */
    public ServiceConfigDesc getServiceConfig(RequestHandler h) {
        ServiceConfigDesc config = serviceConfigsByHandler.get(h);
        if (config == null) {
            config = new ServiceConfigDesc(h);
            serviceConfigsByHandler.putIfAbsent(h, config);
        }
        return config;
    }

    /**
     * This method adds service configuration for given handler
     * 
     * @param h
     * @param config
     */
    public void add(RequestHandler h, ServiceConfigDesc config) {
        Preconditions.requireNotNull(config, "service handler " + h
                + " doesn't define ServiceConfig annotation");
        setServiceConfig(h, config);
    }

    public void setServiceConfig(RequestHandler h, ServiceConfigDesc config) {
        serviceConfigsByHandler.put(h, config);
    }

    public void removeServiceConfig(RequestHandler h) {
        serviceConfigsByHandler.remove(h);
    }
}
