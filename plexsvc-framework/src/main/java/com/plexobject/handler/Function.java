package com.plexobject.handler;

public interface Function<X, Y> {
    Y invoke(X arg);
}
