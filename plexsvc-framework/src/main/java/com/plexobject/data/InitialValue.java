package com.plexobject.data;

public final class InitialValue {
    public static final InitialValue instance = new InitialValue();

    private InitialValue() {
    }

    @Override
    public String toString() {
        return "InitialValue";
    }

}
