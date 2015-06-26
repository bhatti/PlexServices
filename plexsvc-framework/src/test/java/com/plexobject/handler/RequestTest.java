package com.plexobject.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class RequestTest {
    @Test
    public void testCreateUsingDefaultConstructor() throws Exception {
        Request request = new Request();
        assertNull(request.getProtocol());
        assertNull(request.getMethod());
        assertNull(request.getEndpoint());
        assertNull(request.getResponse());
        assertNull(request.getSessionId());
        assertNull(request.getPayload());
    }

    @Test
    public void testCreateUsingConstructor() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.SESSION_ID, "id");
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setProperties(properties)
                .setHeaders(headers).setEndpoint("/w")
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();

        assertEquals(Protocol.HTTP, request.getProtocol());
        assertEquals(RequestMethod.GET, request.getMethod());
        assertEquals("/w", request.getEndpoint());
        assertEquals("id", request.getSessionId());
        assertEquals("{}", request.getPayload());
        assertTrue(request.toString().contains("/w"));
        assertTrue(request.getCreatedAt() <= System.currentTimeMillis());
    }

    @Test
    public void testCreateUsingBuilder() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setSessionId(null)
                .setProperties(properties).setHeaders(headers)
                .setEndpoint("/w").setCodecType(CodecType.JSON)
                .setPayload("{}").setSessionId("id")
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();

        assertEquals(Protocol.HTTP, request.getProtocol());
        assertEquals(RequestMethod.GET, request.getMethod());
        assertEquals("/w", request.getEndpoint());
        assertEquals("id", request.getSessionId());
        assertEquals("{}", request.getPayload());
    }

    @Test
    public void testHandleUnknown() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("p1", "v1");
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setProperties(properties)
                .setHeaders(headers).setEndpoint("/w")
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();

        request.handleUnknown(null, null);
        request.handleUnknown(Constants.PAYLOAD, "payload");
        assertEquals("payload", request.getPayload());
        request.handleUnknown("key", "1");
        assertEquals("1", request.getStringProperty("key"));
        request.handleUnknown("key", 1);
        assertEquals(new Integer(1), request.getProperty("key"));
        request.handleUnknown("key", 1L);
        assertEquals(new Long(1), request.getProperty("key"));
        request.handleUnknown("key", 'k');
        assertEquals(new Character('k'), request.getProperty("key"));
        request.handleUnknown("key", true);
        assertEquals(Boolean.TRUE, request.getProperty("key"));
        request.handleUnknown("key", properties);
        assertEquals("v1", request.getProperty("p1"));
    }

    @Test
    public void testGetSetProperties() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("p1", "v1");
        Map<String, Object> headers = new HashMap<>();
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setProperties(properties)
                .setHeaders(headers).setEndpoint("/w")
                .setCodecType(CodecType.JSON).setPayload("pay")
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        assertEquals("pay", request.getPayload());

        assertTrue(request.hasProperty("p1"));
        assertFalse(request.hasProperty("p2"));
        assertTrue(request.getPropertyNames().contains("p1"));
        assertEquals("v1", request.getStringProperty("p1"));
        request.setProperty("p2", 2);
        assertEquals("2", request.getStringProperty("p2"));
        assertEquals(new Integer(2), request.getIntegerProperty("p2"));
        assertEquals(new Long(2), request.getLongProperty("p2"));
        assertNull(request.getStringProperty("p3"));
        assertNull(request.getLongProperty("p3"));
        request.setProperty("p3", 3L);
        assertEquals(new Long(3), request.getLongProperty("p3"));
        request.setProperty("p3", "3");
        assertEquals(new Long(3), request.getLongProperty("p3"));
        request.setProperty("p4", "true");
        assertTrue(request.getBooleanProperty("p4"));
        request.setProperty("p4", true);
        assertTrue(request.getBooleanProperty("p4", false));
        request.setProperty("p4", 4);
        assertFalse(request.getBooleanProperty("p4", false));
        assertFalse(request.getBooleanProperty("p5", false));

    }

    @Test
    public void testGetSetHeaders() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put(Constants.SESSION_ID, "id");
        Map<String, Object> headers = new HashMap<>();
        headers.put("head1", "val1");
        headers.put("head2", 2);
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setProperties(properties)
                .setHeaders(headers).setEndpoint("/w")
                .setCodecType(CodecType.JSON).setPayload(null)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();

        assertTrue(request.getHeaderNames().contains("head1"));
        assertEquals("val1", request.getHeader("head1"));
        assertEquals("2", request.getHeader("head2"));
        assertNull(request.getHeader("head3"));
        request.setHeader("head3", 3);
        assertEquals("3", request.getHeader("head3"));
    }
}
