package com.plexobject.fsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.plexobject.domain.Pair;
import com.plexobject.util.Preconditions;

/**
 * This class stores transition mappings
 * 
 * @author shahzad bhatti
 *
 */
public class TransitionMappings {
    private final Map<Pair<State, String>, State[]> transitions = new ConcurrentHashMap<>();

    public TransitionMappings() {
    }

    /**
     * This method returns next set of states from given current state and
     * on-event
     * 
     * @param fromState
     * @param onEvent
     * @return set of next states
     * @throws IllegalStateException
     */
    public synchronized State[] getNextStates(State fromState, String onEvent)
            throws IllegalStateException {
        Pair<State, String> key = Pair.of(fromState, onEvent);
        State[] nextStates = transitions.get(key);
        if (nextStates == null || nextStates.length == 0) {
            throw new IllegalStateException(
                    "No next state found for from-state '" + fromState
                            + "', on-event '" + onEvent + "'");
        }
        return nextStates;
    }

    /**
     * This method returns transition mappings
     * 
     * @return
     */
    public synchronized Collection<TransitionMapping> getTransitions() {
        Collection<TransitionMapping> mappings = new HashSet<>();
        for (Map.Entry<Pair<State, String>, State[]> e : transitions.entrySet()) {
            String fromState = e.getKey().first.getName();
            String onEvent = e.getKey().second;
            String[] targetStates = new String[e.getValue().length];
            for (int i = 0; i < e.getValue().length; i++) {
                targetStates[i] = e.getValue()[i].getName();
            }
            mappings.add(new TransitionMapping(fromState, onEvent, targetStates));
        }

        return mappings;
    }

    /**
     * This method adds transition mapping
     * 
     * @param mapping
     */
    public void register(TransitionMapping mapping) {
        register(State.of(mapping.getFromState()), mapping.getOnEvent(),
                toStates(mapping.getTargetStates()));
    }

    /**
     * This method adds transition mapping
     * 
     * @param fromState
     * @param event
     * @param targetStates
     */
    public void register(String fromState, String event, String... targetStates) {
        register(State.of(fromState), event, toStates(targetStates));
    }

    /**
     * This method adds transition mapping
     * 
     * @param fromState
     * @param onEvent
     * @param states
     */
    public synchronized void register(State fromState, String onEvent,
            State[] states) {
        Objects.requireNonNull(fromState, "fromState is required");
        Objects.requireNonNull(onEvent, "onEvent is required");
        Preconditions.checkArgument(
                states != null && states.length > 0,
                "targetStates is required for fromState " + fromState
                        + ", onEvent " + onEvent + ", states "
                        + Arrays.toString(states));
        Pair<State, String> key = Pair.of(fromState, onEvent);
        State[] oldTargetStates = transitions.get(key);
        if (oldTargetStates != null) {
            throw new IllegalStateException("Duplicate transition from state "
                    + fromState + ", onEvent " + onEvent + ", toStates "
                    + states + ", old states " + oldTargetStates);
        }
        State[] targetStates = Arrays.copyOf(states, states.length);
        transitions.put(key, targetStates);
    }

    @Override
    public String toString() {
        return "TransitionMappings [transitions=" + transitions + "]";
    }

    private static State[] toStates(Collection<String> strStates) {
        State[] states = new State[strStates.size()];
        int i = 0;
        for (String state : strStates) {
            states[i++] = State.of(state);
        }
        return states;
    }

    private static State[] toStates(String[] strStates) {
        return toStates(Arrays.asList(strStates));
    }
}
