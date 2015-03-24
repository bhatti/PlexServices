package com.plexobject.fsm;

import java.util.Arrays;
import java.util.Objects;

import com.plexobject.domain.Preconditions;

/**
 * This class defines transition mapping from from-state to target-states for a
 * specified on-event
 * 
 * @author shahzadbhatti
 *
 */
public class TransitionMapping {
    private String fromState;
    private String onEvent;
    private String[] toStates;

    public TransitionMapping() {

    }

    public TransitionMapping(String fromState, String onEvent,
            String... toStates) {
        Objects.requireNonNull(fromState, "fromState is required");
        Objects.requireNonNull(onEvent, "onEvent is required");
        Preconditions.checkArgument(
                toStates != null && toStates.length > 0,
                "toStates is required for fromState " + fromState
                        + ", onEvent " + onEvent + ", states "
                        + Arrays.toString(toStates));
        //
        this.fromState = fromState;
        this.onEvent = onEvent;
        this.toStates = toStates;
    }

    public String getFromState() {
        return fromState;
    }

    public void setFromState(String fromState) {
        this.fromState = fromState;
    }

    public String getOnEvent() {
        return onEvent;
    }

    public void setOnEvent(String onEvent) {
        this.onEvent = onEvent;
    }

    public String[] getToStates() {
        return toStates;
    }

    public void setToStates(String[] toStates) {
        this.toStates = toStates;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fromState.hashCode();
        result = prime * result + onEvent.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TransitionMapping other = (TransitionMapping) obj;
        if (!fromState.equals(other.fromState))
            return false;
        if (!onEvent.equals(other.onEvent))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TransitionMapping [fromState=" + fromState + ", onEvent="
                + onEvent + ", toStates=" + Arrays.toString(toStates) + "]";
    }
}
