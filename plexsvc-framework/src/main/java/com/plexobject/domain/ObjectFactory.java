package com.plexobject.domain;

/**
 * This is general purpose interface for creating objects
 * 
 * @author shahzad bhatti
 *
 * @param <T>
 */
public interface ObjectFactory<T> {
    T create(Object... args);
}
