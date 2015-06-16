package com.plexobject.security;

import com.plexobject.handler.Request;

/**
 * This interface defines method that checks if request is authorized
 * 
 * @author shahzad bhatti
 *
 */
public interface SecurityAuthorizer {
    /**
     * This method validates request for given roles. It should throw
     * AuthException if request is not authorized
     * 
     * @param request
     * @param roles
     * @throws AuthException
     */
    void authorize(Request<Object> request, String[] roles)
            throws AuthException;
}
