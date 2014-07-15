package com.plexobject.bus;

/**
 * This interface defines callback method for notifying when an event is
 * received
 * 
 * @author shahzad bhatti
 *
 */
public interface EventHandler {
    /**
     * This method defines behavior for handling event. It should return back
     * quickly and perform time consuming tasks in another thread.
     * 
     * @param e
     */
    void handle(Event e);
}
