package com.plexobject.service;

import java.util.Collection;

import com.plexobject.handler.BasePayload;
import com.plexobject.handler.Response;

public interface OutgoingInterceptorsLifecycle {

    /**
     * This method adds interceptor for raw JSON/XML output before it's send to
     * client
     * 
     * @param interceptor
     */
    void addOutputInterceptor(Interceptor<BasePayload<Object>> interceptor);

    /**
     * This method remove interceptor for raw JSON/XML output before it's send
     * to client
     * 
     * @param interceptor
     */
    boolean removeOutputInterceptor(Interceptor<BasePayload<Object>> interceptor);

    /**
     * This method returns interceptors for raw JSON/XML output before it's send
     * to client
     * 
     * @return
     */
    Collection<Interceptor<BasePayload<Object>>> getOutputInterceptors();

    /**
     * This method returns true if output interceptors exist
     * 
     * @return
     */
    boolean hasOutputInterceptors();

    /**
     * This method adds interceptor, which allows overriding response object set
     * by handler.
     * 
     * @param interceptor
     */
    void addResponseInterceptor(Interceptor<Response> interceptor);

    /**
     * This method removes interceptor, which allows overriding response object
     * set by handler.
     * 
     * @param interceptor
     */
    boolean removeResponseInterceptor(Interceptor<Response> interceptor);

    /**
     * This method returns response interceptors, which allows overriding
     * response object set by handler.
     * 
     * @return
     */
    Collection<Interceptor<Response>> getResponseInterceptors();

    /**
     * This method returns true if input interceptors exist
     * 
     * @return
     */
    boolean hasResponseInterceptors();

}
