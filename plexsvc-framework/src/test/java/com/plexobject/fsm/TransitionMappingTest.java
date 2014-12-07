package com.plexobject.fsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TransitionMappingTest {
    TransitionMapping instance = new TransitionMapping();

    @Test
    public void testCreateTransitionMapping() {
        TransitionMapping m = new TransitionMapping("from", "event", "to");
        assertEquals("from", m.getFromState());
        assertEquals("event", m.getOnEvent());
        assertEquals("to", m.getTargetStates()[0]);
    }

    @Test
    public void testGetSetFromState() {
        instance.setFromState("from");
        assertEquals("from", instance.getFromState());
    }

    @Test
    public void testGetSetOnEvent() {
        instance.setOnEvent("event");
        assertEquals("event", instance.getOnEvent());
    }

    @Test
    public void testGetSetTargetStates() {
        instance.setOnEvent("to");
        assertEquals("to", instance.getTargetStates()[0]);
    }

    @Test
    public void testHashCode() {
        TransitionMapping m = new TransitionMapping("from", "event", "to");
        assertTrue(m.hashCode() != 0);
    }

    @Test
    public void testEquals() {
        TransitionMapping m1 = new TransitionMapping("from", "event", "to");
        TransitionMapping m2 = new TransitionMapping("from", "event", "to");
        TransitionMapping m3 = new TransitionMapping("from", "event2", "to");
        assertFalse(m1.equals(null));
        assertFalse(m1.equals(3));
        assertFalse(m1.equals(m3));
        assertEquals(m1, m2);
        assertEquals(m1, m1);
    }

    @Test
    public void testToString() {
        TransitionMapping m = new TransitionMapping("from", "event", "to");
        assertTrue(m.toString().contains("TransitionMapping"));
    }

}
