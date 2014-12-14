package com.plexobject.bridge.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.service.Method;

public class WebToJmsEntryTest {
    @Test
    public void testCreateWebToJmsEntry() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/url",
                Method.GET, "destination", 5, false, 1);
        assertEquals(CodecType.JSON, entry.getCodecType());
        assertEquals("/url", entry.getEndpoint());
        assertEquals(Method.GET, entry.getMethod());
        assertEquals("destination", entry.getDestination());
        assertEquals(5, entry.getTimeoutSecs());
    }

    @Test
    public void testGetSetTimeout() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry();
        entry.setTimeoutSecs(5);
        assertEquals(5, entry.getTimeoutSecs());
    }

    @Test
    public void testGetSetDestination() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry();
        entry.setDestination("destination");
        assertEquals("destination", entry.getDestination());
    }

    @Test
    public void testGetSetMethod() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry();
        entry.setMethod(Method.GET);
        assertEquals(Method.GET, entry.getMethod());
    }

    @Test
    public void testGetSetEndpoint() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry();
        entry.setEndpoint("/url");
        assertEquals("/url", entry.getEndpoint());
    }

    @Test
    public void testGetSetRequestType() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry();
        entry.setCodecType(CodecType.JSON);
        assertEquals(CodecType.JSON, entry.getCodecType());
    }
}
