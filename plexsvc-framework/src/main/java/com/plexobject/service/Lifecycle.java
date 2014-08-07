package com.plexobject.service;

/**
 * This interface defines lifecylce methods, which are used by service gatways
 * 
 * @author shahzad bhatti
 *
 */
public interface Lifecycle {
    void start();

    void stop();

    boolean isRunning();
}
