package com.plexobject.service;

import java.util.Collection;
import java.util.Map;

/**
 * This interface defines methods for adding/removing request interceptors
 * 
 * @author shahzad bhatti
 *
 */

public interface InterceptorLifecycle {
    void add(ServiceTypeDesc type, RequestInterceptor interceptor);

    void remove(ServiceTypeDesc type, RequestInterceptor interceptor);

    Map<ServiceTypeDesc, Collection<RequestInterceptor>> getInterceptors();
}
