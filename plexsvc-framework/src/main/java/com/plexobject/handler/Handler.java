package com.plexobject.handler;

/**
 * This interface defines method to handle requests
 * 
 * @author shahzad bhatti
 *
 * @param <T>
 */
public interface Handler<T> {
    void handle(T request);
}
