package com.plexobject.fsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.plexobject.domain.Pair;

/**
 * This class implements simple Finite-State-Machine
 * 
 * @author shahzad bhatti
 *
 */
public class FSM {
    private final TransitionMappings mappings;
    private final TransitionResolver transitionResolver;
    private State currentState;
    private List<Pair<State, String>> breadcrumbs = new ArrayList<>();

    public FSM(State initialState, TransitionMappings mappings,
            TransitionResolver transitionResolver) {
        Objects.requireNonNull(initialState, "initialState is required");
        Objects.requireNonNull(mappings, "transitionMappings is required");
        this.currentState = initialState;
        this.mappings = mappings;
        this.transitionResolver = transitionResolver;
        saveAndReturnState(currentState, null);
    }

    /**
     * FSM maintains current state and this method is used to proceed to the
     * next state
     * 
     * @param onEvent
     * @param args
     *            - optional parameter used for TransitionResolver if needed
     * @return next state
     * @throws IllegalStateException
     */
    public synchronized State nextStateOnEvent(String onEvent, Object args)
            throws IllegalStateException {
        State[] nextStates = mappings.getNextStates(currentState, onEvent);
        if (nextStates.length == 1) {
            return saveAndReturnState(nextStates[0], onEvent);
        }
        if (transitionResolver != null) {
            return saveAndReturnState(transitionResolver.nextState(
                    currentState, onEvent, nextStates, args), onEvent);
        }
        throw new IllegalStateException(
                "Multiple states found for current state " + currentState
                        + " and onEvent " + onEvent
                        + ", but resolver was not set");
    }

    /**
     * This method returns list of states along with event-name that were
     * progressed since FSM was created
     * 
     * @return
     */
    public synchronized List<Pair<State, String>> getBreadCrumbs() {
        return Collections.unmodifiableList(breadcrumbs);
    }

    private synchronized State saveAndReturnState(State state, String onEvent) {
        this.currentState = state;
        if (state != null) {
            breadcrumbs.add(Pair.of(state, onEvent));
        }
        return state;
    }
}
