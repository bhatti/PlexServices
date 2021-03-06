
package com.plexobject.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;

public class ServiceConfigDescTest {
    @ServiceConfig(protocol = Protocol.HTTP, version = "1.0", endpoint = "/w", method = RequestMethod.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request request) {
        }
    }

    @Test
    public void testCreateWithHandlerClass() {
        ServiceConfigDesc desc = new ServiceConfigDesc(WebService.class);
        assertEquals(RequestMethod.GET, desc.method());
        assertEquals(Protocol.HTTP, desc.protocol());
        assertEquals(Void.class, desc.contentsClass());
        assertEquals(CodecType.JSON, desc.codec());
        assertEquals("1.0", desc.version());
        assertEquals("/w", desc.endpoint());
        assertTrue(desc.recordStatsdMetrics());
        assertEquals("employee", desc.rolesAllowed()[0]);
        assertTrue(desc.toString().contains("Desc"));
    }

    @Test
    public void testCreateWithHandlerObject() {
        ServiceConfigDesc desc = new ServiceConfigDesc(new WebService());
        assertEquals(RequestMethod.GET, desc.method());
        assertEquals(Protocol.HTTP, desc.protocol());
        assertEquals(Void.class, desc.contentsClass());
        assertEquals(CodecType.JSON, desc.codec());
        assertEquals("1.0", desc.version());
        assertEquals("/w", desc.endpoint());
        assertTrue(desc.recordStatsdMetrics());
        assertEquals("employee", desc.rolesAllowed()[0]);
        assertTrue(desc.toString().contains("Desc"));
    }

    @Test
    public void testCreateWithHandlerWithBuilder() {
        ServiceConfigDesc.Builder builder = ServiceConfigDesc
                .builder(new WebService());
        builder.setCodecType(CodecType.XML);
        builder.setMethod(RequestMethod.PUT);
        builder.setProtocol(Protocol.WEBSOCKET);
        builder.setContentsClass(Void.class);
        builder.setVersion("2");
        builder.setEndpoint("/ws");
        builder.setRecordStatsdMetrics(false);
        builder.setRolesAllowed(new String[0]);
        ServiceConfigDesc desc = builder.build();
        assertEquals(RequestMethod.PUT, desc.method());
        assertEquals(Protocol.WEBSOCKET, desc.protocol());
        assertEquals(Void.class, desc.contentsClass());
        assertEquals(CodecType.XML, desc.codec());
        assertEquals("2", desc.version());
        assertEquals("/ws", desc.endpoint());
        assertFalse(desc.recordStatsdMetrics());
        assertEquals(0, desc.rolesAllowed().length);
    }

    @Test
    public void testCreateWithWebToJmsEntry() {
        WebToJmsEntry e = new WebToJmsEntry(CodecType.JSON, "/w", RequestMethod.GET,
                "queue://name", 5, false, 1);
        ServiceConfigDesc.Builder builder = ServiceConfigDesc.builder(e);
        ServiceConfigDesc desc = builder.build();
        assertEquals(RequestMethod.GET, desc.method());
        assertEquals(Protocol.HTTP, desc.protocol());
        assertEquals(Void.class, desc.contentsClass());
        assertEquals(CodecType.JSON, desc.codec());
        assertEquals("", desc.version());
        assertEquals("/w", desc.endpoint());
        assertTrue(desc.recordStatsdMetrics());
        assertEquals(0, desc.rolesAllowed().length);
    }
}
