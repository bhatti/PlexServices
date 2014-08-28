package com.plexobject.domain;

public interface ObjectFactory<T> {
    T create(Object... args);
}
