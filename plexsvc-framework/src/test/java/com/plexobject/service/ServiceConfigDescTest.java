package com.plexobject.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;

public class ServiceConfigDescTest {
    @ServiceConfig(protocol = Protocol.HTTP, version = "1.0", endpoint = "/w", method = Method.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request request) {
        }
    }

    @Test
    public void testCreateWithHandler() {
        ServiceConfigDesc desc = new ServiceConfigDesc(WebService.class);
        assertEquals(Method.GET, desc.method());
        assertEquals(Protocol.HTTP, desc.protocol());
        assertEquals(Void.class, desc.payloadClass());
        assertEquals(CodecType.JSON, desc.codec());
        assertEquals("1.0", desc.version());
        assertEquals("/w", desc.endpoint());
        assertTrue(desc.recordStatsdMetrics());
        assertEquals("employee", desc.rolesAllowed()[0]);
        assertTrue(desc.toString().contains("Desc"));
    }
}
