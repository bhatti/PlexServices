package com.plexobject.service;

public interface ServiceHandlerLifecycleMBean {
    void start();

    void stop();

    boolean isRunning();

    String ping();

    String getSummary();
}
