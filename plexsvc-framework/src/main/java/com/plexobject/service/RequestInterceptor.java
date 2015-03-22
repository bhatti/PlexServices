package com.plexobject.service;

import com.plexobject.handler.Request;

/**
 * This interface allows you to override request before it's sent to the handler
 * 
 * @author shahzad bhatti
 *
 */
public interface RequestInterceptor {
    Request intercept(Request request);
}
