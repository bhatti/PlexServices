package com.plexobject.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AuthExceptionTest {
    @Test
    public void testLocationConstructor() {
        AuthException e = new AuthException(200, "sessionId", "location",
                "message");
        assertEquals(200, e.getStatus());
        assertEquals("location", e.getLocation());
        assertEquals("sessionId", e.getSessionId());
    }

    @Test
    public void testMessageConstructor() {
        AuthException e = new AuthException(200, "sessionId", "message");
        assertEquals(200, e.getStatus());
        assertNull(e.getLocation());
        assertEquals("sessionId", e.getSessionId());
    }
}
