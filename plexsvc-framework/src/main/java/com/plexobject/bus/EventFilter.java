package com.plexobject.bus;

import com.plexobject.predicate.Predicate;

/**
 * This method defines an interface for filtering events for subscription
 * 
 * @author shahzad bhatti
 *
 */
public interface EventFilter extends Predicate<Event> {
}
