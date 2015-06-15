package com.plexobject.service;

import java.util.concurrent.Callable;

/**
 * This interceptor before calling underlying service
 * 
 * @author shahzad bhatti
 *
 */
public interface AroundInterceptor {
    /**
     * This method is used to invoke underlying service method
     * 
     * @param service
     * @param method
     * @param caller
     * @return
     * @throws Exception
     */
    Object proceed(Object service, String method, Callable<Object> caller)
            throws Exception;
}
