package com.plexobject.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.jms.Destination;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.plexobject.encode.CodecType;

@RunWith(JMockit.class)
public class JmsResponseDispatcherTest {
    @Mocked
    private IJMSClient jmsClient;
    @Mocked
    private Destination replyTo;

    @SuppressWarnings("unchecked")
    @Test
    public void testDoSend() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsClient, replyTo);

        new Expectations() {
            {
                jmsClient.send(replyTo, (Map<String, Object>) any, "payload");
            }
        };
        d.doSend("payload");
    }

    @Test
    public void testAddSessionId() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsClient, replyTo);
        d.addSessionId("session");
    }

    @Test
    public void testSetStatus() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsClient, replyTo);
        d.setStatus(200);
        assertEquals(200, d.getStatus());
    }

    @Test
    public void testSetCodecType() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsClient, replyTo);
        d.setCodecType(CodecType.JSON);
    }

    @Test
    public void testSetProperty() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsClient, replyTo);
        d.setProperty("name", "value");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSend() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsClient, replyTo);

        new Expectations() {
            {
                jmsClient.send(replyTo, (Map<String, Object>) any, "payload");
            }
        };
        d.send("payload");
    }

    public void testHashCode() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsClient, replyTo);
        assertTrue(d.hashCode() != 0);
        d.addSessionId("session");
        assertTrue(d.hashCode() != 0);
    }

    public void testEquals() throws Exception {
        JmsResponseDispatcher d1 = new JmsResponseDispatcher(jmsClient, replyTo);
        d1.addSessionId("session1");
        JmsResponseDispatcher d2 = new JmsResponseDispatcher(jmsClient, replyTo);
        d2.addSessionId("session2");
        assertFalse(!d1.equals(d2));
        JmsResponseDispatcher d2a = new JmsResponseDispatcher(jmsClient,
                replyTo);
        d2a.addSessionId("session2");
        assertEquals(d1, d2);
    }

}
