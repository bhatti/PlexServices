package com.plexobject.bus;

/**
 * This method defines an interface for filtering events for subscription
 * @author shahzad bhatti
 *
 */
public interface EventFilter {
    /**
     * This method checks if given event should be accepted and sent to subscriber
     * @param e
     * @return true if event should be sent to subscriber, false otherwise
     */
    boolean accept(Event e);
}
