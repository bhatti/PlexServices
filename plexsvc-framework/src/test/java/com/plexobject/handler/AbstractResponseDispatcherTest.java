package com.plexobject.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.http.HttpResponse;
import com.plexobject.security.AuthException;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class AbstractResponseDispatcherTest {
    private String payload;

    private class Dispatcher extends AbstractResponseDispatcher {

        @Override
        public void doSend(Response response, Object encodedReply) {
            super.doSend(response, encodedReply);
            payload = (String) encodedReply;
        }
    }

    private final AbstractResponseDispatcher dispatcher = new Dispatcher();
    private final Request request = EventBusRequest.builder()
            .setProtocol(Protocol.EVENT_BUS).setCodecType(CodecType.JSON)
            .setMethod(RequestMethod.MESSAGE).setResponseDispatcher(dispatcher)
            .setContents("payload").build();

    @Before
    public void setUp() throws Exception {
        request.getResponse().setLocation("loc");
        request.getResponse().setCodecType(CodecType.JSON);
    }

    @Test
    public void testSendError() throws Exception {
        request.getResponse().setContents(
                new AuthException("authCode", "error message"));
        dispatcher.send(request.getResponse());
        assertTrue(payload.contains("\"errorType\":\"AuthException\""));
        assertTrue(payload.contains("\"errorCode\":\"authCode\""));
        assertTrue(payload.contains("\"message\":\"error message\""));
        assertTrue(payload.contains("\"statusCode\":401"));
    }

    @Test
    public void testSendString() throws Exception {
        request.getResponse().setProperty(Constants.SESSION_ID, "session");
        request.getResponse().setContents("hello");
        dispatcher.send(request.getResponse());

        assertEquals("hello", payload);
    }

    @Test
    public void testSendObject() throws Exception {
        request.getResponse().setProperty(Constants.SESSION_ID, "session");
        request.getResponse().setContents(null);
        dispatcher.send(request.getResponse());
        assertNull(payload);

        request.getResponse().setContents(new Date(0));
        dispatcher.send(request.getResponse());

        assertEquals("0", payload);
    }

    @Test
    public void testGetSetStatusCode() throws Exception {
        request.getResponse().setStatusCode(0);
        assertEquals(0, request.getResponse().getStatusCode());
        request.getResponse().setProperty(HttpResponse.STATUS_CODE, 200);
        assertEquals(200, request.getResponse().getStatusCode());
        request.getResponse().setStatusCode(10);
        assertEquals(10, request.getResponse().getStatusCode());
    }

    @Test
    public void testGetSetStatusMessage() throws Exception {
        request.getResponse().setStatusMessage(null);
        assertNull(request.getResponse().getStatusMessage());
        request.getResponse().setProperty(HttpResponse.STATUS_MESSAGE, "xxxx");
        assertEquals("xxxx", request.getResponse().getStatusMessage());
        request.getResponse().setStatusMessage("zzz");
        assertEquals("zzz", request.getResponse().getStatusMessage());
    }

}
