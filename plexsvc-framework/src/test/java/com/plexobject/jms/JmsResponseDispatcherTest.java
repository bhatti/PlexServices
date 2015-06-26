package com.plexobject.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Response;

@RunWith(JMockit.class)
public class JmsResponseDispatcherTest {
    @Mocked
    private JMSContainer jmsContainer;
    @Mocked
    private Destination replyTo;

    @SuppressWarnings("unchecked")
    @Test
    public void testDoSend() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsContainer,
                replyTo);

        new Expectations() {
            {
                jmsContainer
                        .send(replyTo, (Map<String, Object>) any, "payload");
            }
        };

        d.doSend(new Response(null, new HashMap<String, Object>(),
                new HashMap<String, Object>(), "", CodecType.JSON), "payload");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSend() throws Exception {
        JmsResponseDispatcher d = new JmsResponseDispatcher(jmsContainer,
                replyTo);

        new Expectations() {
            {
                jmsContainer
                        .send(replyTo, (Map<String, Object>) any, "payload");
            }
        };
        d.send(new Response(null, new HashMap<String, Object>(),
                new HashMap<String, Object>(), "payload", CodecType.JSON));
    }

}
