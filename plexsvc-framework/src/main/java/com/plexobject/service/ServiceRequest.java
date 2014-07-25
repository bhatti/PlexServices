package com.plexobject.service;

import com.plexobject.security.Authorizable;

public interface ServiceRequest {
    <T> T getProperty(String name);

    <R> R getRequest();

    Authorizable getAuthorizable();

    ServiceResponseBuilder getServiceResponseBuilder();
}
