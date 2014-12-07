package com.plexobject.fsm;

/**
 * This is a callback interface to notify when state is chagned
 * 
 * @author shahzad bhatti
 *
 */
public interface StateChangeListener {
    void stateChanged(State fromState, State newState, String onEvent);
}
