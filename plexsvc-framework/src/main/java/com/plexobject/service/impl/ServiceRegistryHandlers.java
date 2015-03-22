package com.plexobject.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.RequestInterceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceTypeDesc;

/**
 * This is a helper class to manage request handlers and interceptors
 * 
 * @author shahzad bhatti
 *
 */
public class ServiceRegistryHandlers {
    private final ConcurrentHashMap<RequestHandler, ServiceConfigDesc> serviceConfigsByHandler = new ConcurrentHashMap<>();
    private final Map<RequestHandler, Collection<RequestInterceptor>> interceptorsByHandler = new LinkedHashMap<>();
    private final Map<ServiceTypeDesc, Collection<RequestInterceptor>> interceptorsByServiceType = new LinkedHashMap<>();

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
        Objects.requireNonNull(config, "service handler " + h
                + " doesn't define ServiceConfig annotation");
        serviceConfigsByHandler.put(h, config);
        addInterceptors(h);
    }

    /**
     * This method adds interceptor for given service type. The service type can
     * define regex for endpoint
     * 
     * @param type
     * @param interceptor
     */
    public void add(ServiceTypeDesc type, RequestInterceptor interceptor) {
        {
            Collection<RequestInterceptor> interceptors = interceptorsByServiceType
                    .get(type);
            if (interceptors == null) {
                interceptors = new ArrayList<RequestInterceptor>(); // will
                                                                    // preserve
                                                                    // order of
                                                                    // interceptors
                interceptorsByServiceType.put(type, interceptors);
            }
            // check for duplicates because we are using array-lists
            if (!interceptors.contains(interceptor)) {
                interceptors.add(interceptor);
            }
        }
        for (RequestHandler h : serviceConfigsByHandler.keySet()) {
            addInterceptor(type, interceptor, h);
        }
    }

    /**
     * This method removes interceptor for given service type
     * 
     * @param type
     * @param interceptor
     */
    public void remove(ServiceTypeDesc type, RequestInterceptor interceptor) {
        {
            Collection<RequestInterceptor> interceptors = interceptorsByServiceType
                    .get(type);
            if (interceptors != null) {
                interceptors.remove(interceptor);
                if (interceptors.size() == 0) {
                    interceptorsByServiceType.remove(type);
                }
            }
        }
        for (RequestHandler h : new ArrayList<RequestHandler>(
                interceptorsByHandler.keySet())) {
            removeInterceptor(interceptor, h);
        }
    }

    /**
     * This method returns all interceptors for given handler
     * 
     * @param h
     * @return
     */
    public Collection<RequestInterceptor> getInterceptors(RequestHandler h) {
        return interceptorsByHandler.get(h);
    }

    /**
     * This method returns all interceptors
     * 
     * @return
     */
    public Map<ServiceTypeDesc, Collection<RequestInterceptor>> getInterceptors() {
        return new HashMap<ServiceTypeDesc, Collection<RequestInterceptor>>(
                interceptorsByServiceType);
    }

    /**
     * This method removes all interceptors for given handler
     * 
     * @param h
     */
    public void removeInterceptors(RequestHandler h) {
        for (ServiceTypeDesc type : new ArrayList<ServiceTypeDesc>(
                interceptorsByServiceType.keySet())) {
            Collection<RequestInterceptor> interceptors = interceptorsByServiceType
                    .get(type);
            for (RequestInterceptor interceptor : interceptors) {
                removeInterceptor(interceptor, h);
            }
        }
    }

    private void addInterceptor(ServiceTypeDesc type,
            RequestInterceptor interceptor, RequestHandler h) {
        ServiceConfigDesc desc = serviceConfigsByHandler.get(h);
        if (desc.matches(type)) {
            Collection<RequestInterceptor> interceptors = interceptorsByHandler
                    .get(h);
            if (interceptors == null) {
                interceptors = new ArrayList<RequestInterceptor>(); // will
                                                                    // preserve
                                                                    // order
                interceptorsByHandler.put(h, interceptors);
            }
            if (!interceptors.contains(interceptor)) {
                interceptors.add(interceptor);
            }
        }
    }

    private void removeInterceptor(RequestInterceptor interceptor,
            RequestHandler h) {
        Collection<RequestInterceptor> interceptors = interceptorsByHandler
                .get(h);
        if (interceptors != null) {
            interceptors.remove(interceptor);
            if (interceptors.size() == 0) {
                interceptorsByHandler.remove(h);
            }
        }
    }

    private void addInterceptors(RequestHandler h) {
        for (ServiceTypeDesc type : new ArrayList<ServiceTypeDesc>(
                interceptorsByServiceType.keySet())) {
            Collection<RequestInterceptor> interceptors = interceptorsByServiceType
                    .get(type);
            for (RequestInterceptor interceptor : interceptors) {
                addInterceptor(type, interceptor, h);
            }
        }
    }

}
