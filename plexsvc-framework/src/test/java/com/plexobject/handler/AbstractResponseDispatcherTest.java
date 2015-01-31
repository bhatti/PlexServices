package com.plexobject.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.http.HttpResponse;
import com.plexobject.security.AuthException;

public class AbstractResponseDispatcherTest {
    private String session;
    private String payload;

    private class Dispatcher extends AbstractResponseDispatcher {
        @Override
        public void addSessionId(String value) {
            session = value;
        }

        @Override
        public void doSend(String text) {
            super.doSend(text);
            payload = text;
        }
    }

    private final AbstractResponseDispatcher dispatcher = new Dispatcher();

    @Before
    public void setUp() throws Exception {
        dispatcher.setLocation("loc");
        dispatcher.setCodecType(CodecType.JSON);
    }

    @Test
    public void testSendError() throws Exception {
        dispatcher.send(new AuthException("session", "test"));
        assertTrue(payload.contains("\"errorType\":\"AuthException\""));
        assertTrue(payload.contains("\"sessionId\":\"session\""));
        assertTrue(payload.contains("\"errorType\":\"AuthException\""));
        assertTrue(payload.contains("\"status\":401"));
    }

    @Test
    public void testSendString() throws Exception {
        dispatcher.setProperty(Constants.SESSION_ID, "session");
        dispatcher.send("hello");
        assertEquals("hello", payload);
    }

    @Test
    public void testSendObject() throws Exception {
        dispatcher.setProperty(Constants.SESSION_ID, "session");
        dispatcher.send(null);
        assertNull(payload);
        dispatcher.send(new Date(0));
        assertEquals("0", payload);
        assertEquals("session", session);
    }

    @Test
    public void testHashCode() throws Exception {
        assertTrue(dispatcher.hashCode() != 0);
        dispatcher.setProperty(Constants.SESSION_ID, "id");
        assertTrue(dispatcher.hashCode() != 0);
    }

    @Test
    public void testEquals() throws Exception {
        assertFalse(dispatcher.equals(null));
        assertFalse(dispatcher.equals(3));
        assertTrue(dispatcher.equals(dispatcher));
        final AbstractResponseDispatcher other = new Dispatcher();
        assertFalse(dispatcher.equals(other));
        other.setProperty(Constants.SESSION_ID, "id2");
        assertFalse(dispatcher.equals(other));
        other.setProperty(Constants.SESSION_ID, "id");
        assertFalse(dispatcher.equals(other));
        dispatcher.setProperty(Constants.SESSION_ID, "id");
        assertEquals(dispatcher, other);
    }

    @Test
    public void testGetSetStatus() throws Exception {
        dispatcher.setStatus(0);
        assertEquals(0, dispatcher.getStatus());
        dispatcher.setProperty(HttpResponse.STATUS, 200);
        assertEquals(200, dispatcher.getStatus());
        dispatcher.setProperty(HttpResponse.STATUS, "404");
        assertEquals(404, dispatcher.getStatus());
        dispatcher.setProperty(HttpResponse.STATUS, 404L);
        assertEquals(404, dispatcher.getStatus());
        dispatcher.setProperty(HttpResponse.STATUS, new Date());
        assertEquals(0, dispatcher.getStatus());
        dispatcher.setStatus(10);
        assertEquals(10, dispatcher.getStatus());
    }

}
