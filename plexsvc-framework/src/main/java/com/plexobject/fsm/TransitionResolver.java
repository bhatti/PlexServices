package com.plexobject.fsm;

/**
 * This interface would resolve transition when there are multiple target states
 * for given state and event
 * 
 * @author shahzad bhatti
 *
 */
public interface TransitionResolver {
    /**
     * If a combination of state and onEvent results in multiple target states
     * then this method would resolve it
     * 
     * @param currentState
     * @param onEvent
     * @param validStates
     * @param args
     *            - additional parameter passed by the user
     * @return
     */
    State nextState(State currentState, String onEvent, State[] validStates,
            Object args);
}
