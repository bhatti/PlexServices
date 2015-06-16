package com.plexobject.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AuthExceptionTest {
    @Test
    public void testLocationConstructor() {
        AuthException e = new AuthException("code", "message", "location");
        assertEquals(401, e.getStatus());
        assertEquals("location", e.getLocation());
        assertEquals("code", e.getErrorCode());
        assertEquals("message", e.getMessage());
    }

    @Test
    public void testMessageConstructor() {
        AuthException e = new AuthException("code", "message");
        assertEquals(401, e.getStatus());
        assertNull(e.getLocation());
        assertNull(e.getLocation());
        assertEquals("code", e.getErrorCode());
        assertEquals("message", e.getMessage());
    }
}
