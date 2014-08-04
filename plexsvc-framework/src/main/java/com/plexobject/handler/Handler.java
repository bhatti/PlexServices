package com.plexobject.handler;

public interface Handler<T> {
    void handle(T request);
}
