package com.plexobject.fsm;

import com.plexobject.domain.Preconditions;

/**
 * This class encapsulates state of the state-machine
 * 
 * @author shahzad bhatti
 *
 */
public class State {
    private final String name;

    public State(String name) {
        Preconditions.checkEmpty(name, "state is empty");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static State of(String name) {
        return new State(name);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
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
        State other = (State) obj;
        if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
