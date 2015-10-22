package com.plexobject.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AuthExceptionTest {
    @Test
    public void testLocationConstructor() {
        AuthException e = new AuthException("code", "message", "location");
        assertEquals(401, e.getStatusCode());
        assertEquals("location", e.getLocation());
        assertEquals("code", e.getErrorCode());
        assertEquals("message", e.getMessage());
        assertEquals("message", e.getStatusMessage());
    }

    @Test
    public void testMessageConstructor() {
        AuthException e = new AuthException("code", "message");
        assertEquals(401, e.getStatusCode());
        assertNull(e.getLocation());
        assertNull(e.getLocation());
        assertEquals("code", e.getErrorCode());
        assertEquals("message", e.getMessage());
        assertEquals("message", e.getStatusMessage());
    }
}
