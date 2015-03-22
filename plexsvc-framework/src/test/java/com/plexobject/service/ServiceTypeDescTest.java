package com.plexobject.service;

import static org.junit.Assert.*;

import org.junit.Test;

public class ServiceTypeDescTest {
    @Test
    public void testDefaultConstructor() {
        ServiceTypeDesc desc = new ServiceTypeDesc();
        assertNull(desc.method());
        assertNull(desc.protocol());
        assertNull(desc.version());
        assertNull(desc.endpoint());
    }

    @Test
    public void testConstructor() {
        ServiceTypeDesc desc = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/w");
        assertEquals(Method.GET, desc.method());
        assertEquals(Protocol.HTTP, desc.protocol());
        assertEquals("1.0", desc.version());
        assertEquals("/w", desc.endpoint());
        assertTrue(desc.toString().contains("Desc"));
    }

    @Test
    public void testMatches() {
        ServiceTypeDesc desc1 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/w");
        ServiceTypeDesc desc2 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/w");
        ServiceTypeDesc desc3 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/w");
        ServiceTypeDesc desc4 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", null);
        //
        assertTrue(desc1.matches(desc2));
        assertTrue(desc1.matches(new ServiceTypeDesc(null, Method.GET, "1.0",
                "/w")));
        assertFalse(desc1.matches(new ServiceTypeDesc(Protocol.EVENT_BUS,
                Method.GET, "1.0", "/w")));
        //
        assertTrue(desc1.matches(new ServiceTypeDesc(Protocol.HTTP, null,
                "1.0", "/w")));
        assertFalse(desc1.matches(new ServiceTypeDesc(Protocol.HTTP,
                Method.POST, "1.0", "/w")));
        //
        assertTrue(desc1.matches(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/w")));
        assertFalse(desc1.matches(new ServiceTypeDesc(Protocol.HTTP,
                Method.GET, "1.1", "/w")));
        assertFalse(desc3.matches(new ServiceTypeDesc(Protocol.HTTP,
                Method.GET, "1.1", "/w")));
        assertTrue(desc3.matches(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/w")));
        //
        assertTrue(desc1.matches(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "*")));
        assertTrue(desc1.matches(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/*")));
        assertTrue(desc1.matches(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/.*")));
        assertTrue(desc1.matches(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/?*")));
        assertFalse(desc1.matches(new ServiceTypeDesc(Protocol.HTTP,
                Method.GET, "1.0", "/?")));
        assertFalse(desc3.matches(new ServiceTypeDesc(Protocol.HTTP,
                Method.GET, "1.0", null)));
        assertFalse(desc4.matches(new ServiceTypeDesc(Protocol.HTTP,
                Method.GET, "1.0", "/z")));
        assertTrue(desc4.matches(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", null)));
    }

    @Test
    public void testHashcode() {
        ServiceTypeDesc desc1 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/w");
        ServiceTypeDesc desc2 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/w");
        ServiceTypeDesc desc3 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/w");
        ServiceTypeDesc desc4 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", null);
        //
        assertTrue(desc1.hashCode() != 0);
        assertTrue(desc2.hashCode() != 0);
        assertTrue(desc3.hashCode() != 0);
        assertTrue(desc4.hashCode() != 0);
    }

    @Test
    public void testEquals() {
        ServiceTypeDesc desc1 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/w");
        ServiceTypeDesc desc2 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/w");
        ServiceTypeDesc desc3 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/w");
        ServiceTypeDesc desc4 = new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", null);
        //
        assertFalse(desc1.equals(null));
        assertFalse(desc1.equals(3));
        assertTrue(desc1.equals(desc1));
        assertTrue(desc1.equals(desc2));
        assertFalse(desc1.equals(new ServiceTypeDesc(null, Method.GET, "1.0",
                "/w")));
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.EVENT_BUS,
                Method.GET, "1.0", "/w")));
        //
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, null,
                "1.0", "/w")));
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP,
                Method.POST, "1.0", "/w")));
        //
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/w")));
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.1", "/w")));
        assertFalse(desc3.matches(new ServiceTypeDesc(Protocol.HTTP,
                Method.GET, "1.1", "/w")));
        assertTrue(desc3.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                null, "/w")));
        //
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "*")));
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/*")));
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/.*")));
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/?*")));
        assertFalse(desc1.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/?")));
        assertFalse(desc3.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", null)));
        assertFalse(desc4.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", "/z")));
        assertTrue(desc4.equals(new ServiceTypeDesc(Protocol.HTTP, Method.GET,
                "1.0", null)));
    }

}
