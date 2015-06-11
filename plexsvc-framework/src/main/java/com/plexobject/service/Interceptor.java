package com.plexobject.service;

public interface Interceptor<T> {
    T intercept(T object);

}
