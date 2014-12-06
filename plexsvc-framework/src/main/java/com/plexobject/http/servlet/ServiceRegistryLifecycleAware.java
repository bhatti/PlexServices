package com.plexobject.http.servlet;

import com.plexobject.service.ServiceRegistry;

/**
 * This callback is used to notify when service registry is started/stopped
 * 
 * @author shahzad bhatti
 *
 */
public interface ServiceRegistryLifecycleAware {
    /**
     * This method is called when service registry is started
     * 
     * @param serviceRegistry
     */
    void onStarted(ServiceRegistry serviceRegistry);

    /**
     * This method is called when service registry is stopped
     * 
     * @param serviceRegistry
     */
    void onStopped(ServiceRegistry serviceRegistry);
}
