package com.plexobject.fsm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StateTest {
    @Test
    public void testGetName() {
        State instance = new State("name");
        assertEquals("name", instance.getName());
    }
}
