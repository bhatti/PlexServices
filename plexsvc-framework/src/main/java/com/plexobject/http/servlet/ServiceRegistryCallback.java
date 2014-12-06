package com.plexobject.http.servlet;

import com.plexobject.service.ServiceRegistry;

/**
 * This callback is used to add request handlers when service-registry is
 * created
 * 
 * @author shahzad bhatti
 *
 */
public interface ServiceRegistryCallback {
    void created(ServiceRegistry serviceRegistry);
}
