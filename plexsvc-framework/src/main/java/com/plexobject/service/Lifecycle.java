package com.plexobject.service;

/**
 * This interface defines lifecycle methods, which are used by service gateways
 * 
 * @author shahzad bhatti
 *
 */
public interface Lifecycle {
    void start();

    void stop();

    boolean isRunning();
}
