package com.plexobject.service;

/**
 * This class is implemented by services to be notified when gatways are
 * started/stopped.
 * 
 * @author shahzad bhatti
 *
 */
public interface LifecycleAware {
    void onCreated();

    void onDestroyed();

    void onStarted();

    void onStopped();
}
