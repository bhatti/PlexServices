package com.plexobject.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.RequestInterceptor;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceTypeDesc;

public class ServiceRegistryHandlers {
    private final Map<RequestHandler, ServiceConfigDesc> serviceConfigsByHandler = new ConcurrentHashMap<>();
    private final Map<RequestHandler, Collection<RequestInterceptor>> interceptorsByHandler = new LinkedHashMap<>();
    private final Map<ServiceTypeDesc, Collection<RequestInterceptor>> interceptorsByServiceType = new LinkedHashMap<>();

    public ServiceConfigDesc getServiceConfig(RequestHandler h) {
        ServiceConfigDesc config = serviceConfigsByHandler.get(h);
        if (config == null) {
            config = new ServiceConfigDesc(h);
        }
        return config;
    }

    public void add(RequestHandler h, ServiceConfigDesc config) {
        Objects.requireNonNull(config, "service handler " + h
                + " doesn't define ServiceConfig annotation");
        serviceConfigsByHandler.put(h, config);
        addInterceptors(h);
    }

    public void add(ServiceTypeDesc type, RequestInterceptor interceptor) {
        {
            Collection<RequestInterceptor> interceptors = interceptorsByServiceType
                    .get(type);
            if (interceptors == null) {
                interceptors = new HashSet<RequestInterceptor>();
                interceptorsByServiceType.put(type, interceptors);
            }
            interceptors.add(interceptor);
        }
        for (RequestHandler h : serviceConfigsByHandler.keySet()) {
            _addInterceptor(type, interceptor, h);
        }
    }

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
            _removeInterceptor(interceptor, h);
        }
    }

    public Collection<RequestInterceptor> getInterceptors(RequestHandler h) {
        return interceptorsByHandler.get(h);
    }

    public void removeInterceptors(RequestHandler h) {
        for (ServiceTypeDesc type : new ArrayList<ServiceTypeDesc>(
                interceptorsByServiceType.keySet())) {
            Collection<RequestInterceptor> interceptors = interceptorsByServiceType
                    .get(type);
            for (RequestInterceptor interceptor : interceptors) {
                _removeInterceptor(interceptor, h);
            }
        }
    }

    private void _addInterceptor(ServiceTypeDesc type,
            RequestInterceptor interceptor, RequestHandler h) {
        ServiceConfigDesc desc = serviceConfigsByHandler.get(h);
        if (desc.matches(type)) {
            Collection<RequestInterceptor> interceptors = interceptorsByHandler
                    .get(h);
            if (interceptors == null) {
                interceptors = new HashSet<RequestInterceptor>();
                interceptorsByHandler.put(h, interceptors);
            }
            interceptors.add(interceptor);
        }
    }

    private void _removeInterceptor(RequestInterceptor interceptor,
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
                _addInterceptor(type, interceptor, h);
            }
        }
    }

}
