package com.plexobject.bridge.eb;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jms.MessageConsumer;
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
import com.plexobject.jms.JmsClient;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;

@RunWith(JMockit.class)
public class EventBusToJmsBridgeTest {
    public static class TestUser {

    }

    @Mocked
    private JmsClient jmsClient;
    @Mocked
    private EventBus eb;
    @Mocked
    private MessageConsumer consumer;
    @Mocked
    private TextMessage message;
    private EventBusToJmsBridge bridge;

    @Before
    public void setUp() throws Exception {
        bridge = new EventBusToJmsBridge(jmsClient,
                new ArrayList<EventBusToJmsEntry>(), eb);
    }

    @Test
    public void testAddRemoveJmsToEB() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue:{scope}-query-user-service-queue", "query-user-channel",
                TestUser.class.getName());
        bridge.add(entry);

        assertNotNull(bridge.getJmsListener(entry));
        bridge.remove(entry);
        assertNull(bridge.getJmsListener(entry));
    }

    @Test
    public void testAddRemoveEBToJms() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue:{scope}-assign-bugreport-service-queue",
                TestUser.class.getName());
        bridge.add(entry);
        assertNotNull(bridge.getEBListener(entry));
        bridge.remove(entry);
        assertNull(bridge.getEBListener(entry));
    }

    @Test
    public void testStartStopJmsToEB() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue:{scope}-query-user-service-queue", "query-user-channel",
                TestUser.class.getName());
        bridge.add(entry);
        final JmsListener listener = bridge.getJmsListener(entry);
        new Expectations() {
            {
                jmsClient.start();
                jmsClient
                        .createConsumer("queue:{scope}-query-user-service-queue");
                returns(consumer);
                consumer.setMessageListener(listener);
                jmsClient.stop();
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
                "queue:{scope}-query-user-service-queue", "query-user-channel",
                TestUser.class.getName());
        bridge.add(entry);
        JmsListener listener = bridge.getJmsListener(entry);
        new Expectations() {
            {
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
                "queue:{scope}-assign-bugreport-service-queue",
                TestUser.class.getName());
        bridge.add(entry);
        EBListener listener = bridge.getEBListener(entry);
        new Expectations() {
            {
                jmsClient.send("queue:{scope}-assign-bugreport-service-queue",
                        (Map<String, Object>) any, anyString);
            }
        };
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });
        listener.handle(request);
    }

    @Test
    public void testStartStopEBToJms() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue:{scope}-assign-bugreport-service-queue",
                TestUser.class.getName());
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
