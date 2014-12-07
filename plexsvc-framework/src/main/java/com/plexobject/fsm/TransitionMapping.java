package com.plexobject.fsm;

import java.util.Arrays;
import java.util.Objects;

import com.plexobject.util.Preconditions;

public class TransitionMapping {
    private String fromState;
    private String onEvent;
    private String[] targetStates;

    public TransitionMapping() {

    }

    public TransitionMapping(String fromState, String onEvent,
            String... targetStates) {
        Objects.requireNonNull(fromState, "fromState is required");
        Objects.requireNonNull(onEvent, "onEvent is required");
        Preconditions.checkArgument(
                targetStates != null && targetStates.length > 0,
                "targetStates is required for fromState " + fromState
                        + ", onEvent " + onEvent + ", states "
                        + Arrays.toString(targetStates));
        //
        this.fromState = fromState;
        this.onEvent = onEvent;
        this.targetStates = targetStates;
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

    public String[] getTargetStates() {
        return targetStates;
    }

    public void setTargetStates(String[] targetStates) {
        this.targetStates = targetStates;
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
                + onEvent + ", targetStates=" + Arrays.toString(targetStates)
                + "]";
    }
}
