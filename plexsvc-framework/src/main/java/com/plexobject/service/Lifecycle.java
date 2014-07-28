package com.plexobject.service;

public interface Lifecycle {
    void start();

    void stop();

    boolean isRunning();
}
