package com.plexobject.service;

/**
 * This interface defines methods for adding/removing request interceptors
 * 
 * @author shahzad bhatti
 *
 */

public interface InterceptorLifecycle {
    void add(ServiceTypeDesc type, RequestInterceptor interceptor);

    void remove(ServiceTypeDesc type, RequestInterceptor interceptor);

}
