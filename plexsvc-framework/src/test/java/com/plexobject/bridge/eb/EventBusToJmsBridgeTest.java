package com.plexobject.bridge.eb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.TextMessage;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.plexobject.bridge.eb.EventBusToJmsBridge.EBListener;
import com.plexobject.bridge.eb.EventBusToJmsBridge.JmsListener;
import com.plexobject.bus.EventBus;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

@RunWith(JMockit.class)
public class EventBusToJmsBridgeTest {
    public static class TestUser {

    }

    @Mocked
    private JMSContainer jmsContainer;
    @Mocked
    private EventBus eb;
    @Mocked
    private Closeable consumer;
    @Mocked
    private TextMessage message;
    private EventBusToJmsBridge bridge;

    @Before
    public void setUp() throws Exception {
        bridge = new EventBusToJmsBridge(jmsContainer,
                new ArrayList<EventBusToJmsEntry>(), eb);
    }

    @Test
    public void testAddRemoveJmsToEB() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue://{scope}-query-user-service-queue",
                "query-user-channel", TestUser.class.getName(), 1, 0);
        bridge.add(entry);

        assertNotNull(bridge.getJmsListener(entry));
        bridge.remove(entry);
        assertNull(bridge.getJmsListener(entry));
    }

    @Test
    public void testAddRemoveEBToJms() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue://{scope}-assign-bugreport-service-queue",
                TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        assertNotNull(bridge.getEBListener(entry));
        bridge.remove(entry);
        assertNull(bridge.getEBListener(entry));
    }

    @Test
    public void testStartStopJmsToEB() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue://{scope}-query-user-service-queue",
                "query-user-channel", TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        final JmsListener listener = bridge.getJmsListener(entry);
        new Expectations() {
            {
                jmsContainer.start();
                Destination destination = jmsContainer
                        .getDestination("queue://{scope}-query-user-service-queue");
                jmsContainer.setMessageListener(destination, listener,
                        (MessageListenerConfig) any);
                returns(consumer);
                jmsContainer.stop();
                consumer.close();
            }
        };
        bridge.start();
        bridge.stop();
    }

    @Test
    public void testOnMessageJmsToEB() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue://{scope}-query-user-service-queue",
                "query-user-channel", TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        JmsListener listener = bridge.getJmsListener(entry);
        new Expectations() {
            {
                message.getPropertyNames();
                message.getText();
                returns("{}");
                message.getJMSReplyTo();
                returns(null);
                eb.publish("query-user-channel", (Request) any);
            }
        };
        listener.onMessage(message);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleEBToJms() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue://{scope}-assign-bugreport-service-queue",
                TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        EBListener listener = bridge.getEBListener(entry);
        new Expectations() {
            {
                Destination dest = jmsContainer
                        .getDestination("queue://{scope}-assign-bugreport-service-queue");
                jmsContainer.send(dest, (Map<String, Object>) any, anyString);
            }
        };
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        Request request = Request
                .builder()
                .setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET)
                .setEndpoint("/w")
                .setProperties(properties)
                .setHeaders(headers)
                .setContents(payload)
                .setCodecType(CodecType.JSON)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        listener.handle(request);
    }

    @Test
    public void testStartStopEBToJms() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue://{scope}-assign-bugreport-service-queue",
                TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        final EBListener listener = bridge.getEBListener(entry);
        new Expectations() {
            {
                eb.subscribe("create-user", listener, null);
                returns(101L);
                eb.unsubscribe(101L);
            }
        };
        bridge.start();
        bridge.stop();
    }
}
