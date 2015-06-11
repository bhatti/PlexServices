package com.plexobject.service;

import java.util.Collection;

import com.plexobject.handler.Request;

/**
 * This interface allows adding/removing interceptors for input
 * 
 * @author shahzad bhatti
 *
 */
public interface IncomingInterceptorsLifecycle {

    /**
     * This method adds interceptor for raw JSON/XML input before it's decoded
     * into object
     * 
     * @param interceptor
     */
    void addInputInterceptor(Interceptor<String> interceptor);

    /**
     * This method remove interceptor for raw JSON/XML input before it's decoded
     * into object
     * 
     * @param interceptor
     */
    boolean removeInputInterceptor(Interceptor<String> interceptor);

    /**
     * This method returns interceptors for raw JSON/XML input before it's
     * decoded into object
     * 
     * @return
     */
    Collection<Interceptor<String>> getInputInterceptors();

    /**
     * This method returns true if input interceptors exist
     * 
     * @return
     */
    boolean hasInputInterceptors();

    /**
     * This method adds interceptor, which is invoked before passing request is
     * passed to handler
     * 
     * @param interceptor
     */
    void addRequestInterceptor(Interceptor<Request> interceptor);

    /**
     * This method removes interceptor, which is invoked before passing request
     * is passed to handler
     * 
     * @param interceptor
     */
    boolean removeRequestInterceptor(Interceptor<Request> interceptor);

    /**
     * This method returns request interceptors, which is invoked before passing
     * request is passed to handler
     * 
     * @return
     */
    Collection<Interceptor<Request>> getRequestInterceptors();

    /**
     * This method returns true if request interceptors exist
     * 
     * @return
     */
    boolean hasRequestInterceptors();

}
