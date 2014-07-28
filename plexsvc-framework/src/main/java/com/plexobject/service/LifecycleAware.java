package com.plexobject.service;

public interface LifecycleAware {
    void onCreated();

    void onDestroyed();

    void onStarted();

    void onStopped();
}
