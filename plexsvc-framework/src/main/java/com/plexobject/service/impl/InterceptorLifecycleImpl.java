package com.plexobject.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.plexobject.handler.BasePayload;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.service.AroundInterceptor;
import com.plexobject.service.Interceptor;
import com.plexobject.service.InterceptorsLifecycle;

public class InterceptorLifecycleImpl implements InterceptorsLifecycle {
    private final List<Interceptor<Request>> requestInterceptors = new ArrayList<>();
    private final List<Interceptor<Response>> responseInterceptors = new ArrayList<>();
    private final List<Interceptor<BasePayload<Object>>> inputInterceptors = new ArrayList<>();
    private final List<Interceptor<BasePayload<Object>>> outputInterceptors = new ArrayList<>();
    private AroundInterceptor aroundInterceptor;
    private boolean hasRequestInterceptors;
    private boolean hasResponseInterceptors;
    private boolean hasInputInterceptors;
    private boolean hasOutputInterceptors;

    /**
     * This method adds interceptor, which is invoked before passing request is
     * passed to handler
     * 
     * @param interceptor
     */
    @Override
    public synchronized void addRequestInterceptor(
            Interceptor<Request> interceptor) {
        if (!requestInterceptors.contains(interceptor)) {
            requestInterceptors.add(interceptor);
        }
        hasRequestInterceptors = requestInterceptors.size() > 0;
    }

    /**
     * This method removes interceptor, which is invoked before passing request
     * is passed to handler
     * 
     * @param interceptor
     */
    @Override
    public synchronized boolean removeRequestInterceptor(
            Interceptor<Request> interceptor) {
        int ndx = requestInterceptors.indexOf(interceptor);
        if (ndx != -1) {
            requestInterceptors.remove(ndx);
        }
        hasRequestInterceptors = requestInterceptors.size() > 0;

        return ndx != -1;
    }

    /**
     * This method returns request interceptors, which is invoked before passing
     * request is passed to handler
     * 
     * @return
     */
    @Override
    public synchronized Collection<Interceptor<Request>> getRequestInterceptors() {
        return requestInterceptors;
    }

    /**
     * This method adds interceptor, which allows overriding response object set
     * by handler.
     * 
     * @param interceptor
     */
    @Override
    public synchronized void addResponseInterceptor(
            Interceptor<Response> interceptor) {
        if (!responseInterceptors.contains(interceptor)) {
            responseInterceptors.add(interceptor);
        }
        hasResponseInterceptors = responseInterceptors.size() > 0;

    }

    /**
     * This method removes interceptor, which allows overriding response object
     * set by handler.
     * 
     * @param interceptor
     */
    @Override
    public synchronized boolean removeResponseInterceptor(
            Interceptor<Response> interceptor) {
        int ndx = responseInterceptors.indexOf(interceptor);
        if (ndx != -1) {
            responseInterceptors.remove(ndx);
        }
        hasResponseInterceptors = responseInterceptors.size() > 0;

        return ndx != -1;
    }

    /**
     * This method returns response interceptors, which allows overriding
     * response object set by handler.
     * 
     * @return
     */
    @Override
    public synchronized Collection<Interceptor<Response>> getResponseInterceptors() {
        return responseInterceptors;
    }

    /**
     * This method adds interceptor for raw JSON/XML input before it's decoded
     * into object
     * 
     * @param interceptor
     */
    @Override
    public synchronized void addInputInterceptor(
            Interceptor<BasePayload<Object>> interceptor) {
        if (!inputInterceptors.contains(interceptor)) {
            inputInterceptors.add(interceptor);
        }
        hasInputInterceptors = inputInterceptors.size() > 0;
    }

    /**
     * This method remove interceptor for raw JSON/XML input before it's decoded
     * into object
     * 
     * @param interceptor
     */
    @Override
    public synchronized boolean removeInputInterceptor(
            Interceptor<BasePayload<Object>> interceptor) {
        int ndx = inputInterceptors.indexOf(interceptor);
        if (ndx != -1) {
            inputInterceptors.remove(ndx);
        }
        hasInputInterceptors = inputInterceptors.size() > 0;

        return ndx != -1;
    }

    /**
     * This method returns interceptors for raw JSON/XML input before it's
     * decoded into object
     * 
     * @return
     */
    @Override
    public synchronized Collection<Interceptor<BasePayload<Object>>> getInputInterceptors() {
        return inputInterceptors;
    }

    /**
     * This method adds interceptor for raw JSON/XML output before it's send to
     * client
     * 
     * @param interceptor
     */
    @Override
    public synchronized void addOutputInterceptor(
            Interceptor<BasePayload<Object>> interceptor) {
        if (!outputInterceptors.contains(interceptor)) {
            outputInterceptors.add(interceptor);
        }
        hasOutputInterceptors = outputInterceptors.size() > 0;
    }

    /**
     * This method remove interceptor for raw JSON/XML output before it's send
     * to client
     * 
     * @param interceptor
     */
    @Override
    public synchronized boolean removeOutputInterceptor(
            Interceptor<BasePayload<Object>> interceptor) {
        int ndx = outputInterceptors.indexOf(interceptor);
        if (ndx != -1) {
            outputInterceptors.remove(ndx);
        }
        hasOutputInterceptors = outputInterceptors.size() > 0;
        return ndx != -1;
    }

    /**
     * This method returns interceptors for raw JSON/XML output before it's send
     * to client
     * 
     * @return
     */
    @Override
    public synchronized Collection<Interceptor<BasePayload<Object>>> getOutputInterceptors() {
        return outputInterceptors;
    }

    @Override
    public synchronized boolean hasInputInterceptors() {
        return hasInputInterceptors;
    }

    @Override
    public synchronized boolean hasRequestInterceptors() {
        return hasRequestInterceptors;
    }

    @Override
    public synchronized boolean hasOutputInterceptors() {
        return hasOutputInterceptors;
    }

    @Override
    public synchronized boolean hasResponseInterceptors() {
        return hasResponseInterceptors;
    }

    @Override
    public void setAroundInterceptor(AroundInterceptor interceptor) {
        this.aroundInterceptor = interceptor;

    }

    @Override
    public AroundInterceptor getAroundInterceptor() {
        return aroundInterceptor;
    }

}
