package com.plexobject.service;

/**
 * This interface defines methods for adding/removing interceptors
 * 
 * @author shahzad bhatti
 *
 */

public interface InterceptorsLifecycle extends IncomingInterceptorsLifecycle,
        OutgoingInterceptorsLifecycle {
    /**
     * This method returns around-interceptor that wraps execution of underlying
     * service invocation
     * 
     * @return
     */
    AroundInterceptor getAroundInterceptor();

    /**
     * This interceptor is called around the underlying service invocation
     * 
     * @param interceptor
     */
    void setAroundInterceptor(AroundInterceptor interceptor);
}
