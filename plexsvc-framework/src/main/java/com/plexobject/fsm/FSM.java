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
    private List<StateChangeListener> listeners = new ArrayList<>();
    private Object lock = new Object();

    public FSM(State initialState, TransitionMappings mappings,
            TransitionResolver transitionResolver) {
        Objects.requireNonNull(initialState, "initialState is required");
        Objects.requireNonNull(mappings, "transitionMappings is required");
        this.currentState = initialState;
        this.mappings = mappings;
        this.transitionResolver = transitionResolver;
        setCurrentStateAndBreadcrumbs(currentState, null);
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
    public State nextStateOnEvent(String onEvent, Object args)
            throws IllegalStateException {
        State oldState = null;
        State newState = null;
        List<StateChangeListener> copy = null;
        //

        synchronized (lock) {
            oldState = currentState;
            State[] nextStates = mappings.getNextStates(currentState, onEvent);
            if (nextStates == null || nextStates.length == 0) {
                throw new IllegalStateException(
                        "No next state found for from-state '" + currentState
                                + "', on-event '" + onEvent + "'");
            } else if (nextStates.length == 1) {
                newState = setCurrentStateAndBreadcrumbs(nextStates[0], onEvent);
            } else if (transitionResolver != null) {
                newState = setCurrentStateAndBreadcrumbs(transitionResolver.nextState(
                        currentState, onEvent, nextStates, args), onEvent);
            } else {
                throw new IllegalStateException(
                        "Multiple states found for current state "
                                + currentState + " and onEvent " + onEvent
                                + ", but resolver was not set");
            }
            copy = new ArrayList<>(listeners);
        }
        for (StateChangeListener l : copy) {
            l.stateChanged(oldState, newState, onEvent);
        }
        return newState;
    }

    /**
     * This method returns list of states along with event-name that were
     * progressed since FSM was created
     * 
     * @return
     */
    public List<Pair<State, String>> getBreadCrumbs() {
        synchronized (lock) {
            return Collections.unmodifiableList(breadcrumbs);
        }
    }

    public void addStateChangeListener(StateChangeListener l) {
        synchronized (lock) {
            if (!this.listeners.contains(l)) {
                this.listeners.add(l);
            }
        }
    }

    public boolean removeStateChangeListener(StateChangeListener l) {
        synchronized (lock) {
            return this.listeners.remove(l);
        }
    }

    private State setCurrentStateAndBreadcrumbs(State newState, String onEvent) {
        this.currentState = newState;
        breadcrumbs.add(Pair.of(newState, onEvent));
        return newState;
    }
}
