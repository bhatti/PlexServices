package com.plexobject.fsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.plexobject.domain.Pair;
import com.plexobject.domain.Preconditions;

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
    private List<Pair<State, String>> history = new ArrayList<>();
    private List<StateChangeListener> listeners = new ArrayList<>();
    private Object lock = new Object();

    public FSM(State initialState, TransitionMappings mappings,
            TransitionResolver transitionResolver) {
        Preconditions.requireNotNull(initialState, "initialState is required");
        Preconditions
                .requireNotNull(mappings, "transitionMappings is required");
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
        List<StateChangeListener> copyListeners = null;
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
                newState = setCurrentStateAndBreadcrumbs(
                        transitionResolver.nextState(currentState, onEvent,
                                nextStates, args), onEvent);
            } else {
                throw new IllegalStateException(
                        "Multiple states found for current state "
                                + currentState + " and onEvent " + onEvent
                                + ", but resolver was not set");
            }
            if (listeners.size() > 0) {
                copyListeners = new ArrayList<>(listeners);
            }
        }
        if (copyListeners != null) {
            notifyListeners(onEvent, oldState, newState, copyListeners);
        }
        return newState;
    }

    /**
     * This method returns current state
     * 
     * @return
     */
    public State getCurrentState() {
        synchronized (lock) {
            return currentState;
        }
    }

    /**
     * This method returns list of states along with event-name that were
     * progressed since FSM was created
     * 
     * @return
     */
    public List<Pair<State, String>> getHistory() {
        synchronized (lock) {
            return Collections.unmodifiableList(history);
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

    private void notifyListeners(String onEvent, State oldState,
            State newState, List<StateChangeListener> copyListeners) {
        for (StateChangeListener l : copyListeners) {
            l.stateChanged(oldState, newState, onEvent);
        }
    }

    private State setCurrentStateAndBreadcrumbs(State newState, String onEvent) {
        this.currentState = newState;
        history.add(Pair.of(newState, onEvent));
        return newState;
    }
}
