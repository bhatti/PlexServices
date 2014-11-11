package com.plexobject.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

public class ServiceConfigDescTest {
    @ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, version = "1.0", endpoint = "/w", method = Method.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request request) {
        }
    }

    @Test
    public void testCreateWithHandler() {
        ServiceConfigDesc desc = new ServiceConfigDesc(WebService.class);
        assertEquals(Method.GET, desc.getMethod());
        assertEquals(GatewayType.HTTP, desc.getGatewayType());
        assertEquals(Void.class, desc.getRequestClass());
        assertEquals(CodecType.JSON, desc.getCodecType());
        assertEquals("1.0", desc.getVersion());
        assertEquals("/w", desc.getEndpoint());
        assertTrue(desc.isRecordStatsdMetrics());
        assertEquals("employee", desc.getRolesAllowed()[0]);
        assertTrue(desc.toString().contains("Desc"));
    }
}
