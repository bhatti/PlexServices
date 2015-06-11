package com.plexobject.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.http.HttpResponse;
import com.plexobject.security.AuthException;

public class AbstractResponseDispatcherTest {
    private String payload;

    private class Dispatcher extends AbstractResponseDispatcher {

        @Override
        public void doSend(Response response, String text) {
            super.doSend(response, text);
            payload = text;
        }
    }

    private final AbstractResponseDispatcher dispatcher = new Dispatcher();
    private final Response response = new Response(
            new HashMap<String, Object>(), new HashMap<String, Object>(),
            "payload");

    @Before
    public void setUp() throws Exception {
        response.setLocation("loc");
        response.setCodecType(CodecType.JSON);
    }

    @Test
    public void testSendError() throws Exception {
        response.setPayload(new AuthException("session", "test"));
        dispatcher.send(response);
        assertTrue(payload.contains("\"errorType\":\"AuthException\""));
        assertTrue(payload.contains("\"sessionId\":\"session\""));
        assertTrue(payload.contains("\"errorType\":\"AuthException\""));
        assertTrue(payload.contains("\"status\":401"));
    }

    @Test
    public void testSendString() throws Exception {
        response.setProperty(Constants.SESSION_ID, "session");
        response.setPayload("hello");
        dispatcher.send(response);

        assertEquals("hello", payload);
    }

    @Test
    public void testSendObject() throws Exception {
        response.setProperty(Constants.SESSION_ID, "session");
        response.setPayload(null);
        dispatcher.send(response);
        assertNull(payload);

        response.setPayload(new Date(0));
        dispatcher.send(response);

        assertEquals("0", payload);
    }

    @Test
    public void testGetSetStatus() throws Exception {
        response.setStatus(0);
        assertEquals(0, response.getStatus());
        response.setProperty(HttpResponse.STATUS, 200);
        assertEquals(200, response.getStatus());
        response.setStatus(10);
        assertEquals(10, response.getStatus());
    }

}
