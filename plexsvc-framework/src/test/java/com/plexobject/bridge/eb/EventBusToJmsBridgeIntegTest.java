package com.plexobject.bridge.eb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.bridge.eb.EventBusToJmsBridge.JmsListener;
import com.plexobject.bus.EventBus;
import com.plexobject.bus.impl.EventBusImpl;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSTestUtils;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;

public class EventBusToJmsBridgeIntegTest {
    public static class TestUser {
        private String name;

        public TestUser() {
        }

        public TestUser(String name) {
            this.setName(name);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private JMSContainer jmsContainer;
    private EventBus eb;
    private EventBusToJmsBridge bridge;
    private static Properties properties = new Properties();
    private static BrokerService broker;

    @Before
    public void setUp() throws Exception {
        jmsContainer = JMSUtils.getJMSContainer(new Configuration(properties));
        eb = new EventBusImpl();
        bridge = new EventBusToJmsBridge(jmsContainer,
                new ArrayList<EventBusToJmsEntry>(), eb);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        broker = JMSTestUtils.startBroker(properties);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        broker.stop();
    }

    @Test
    public void testRun() throws Exception {
        EventBusToJmsEntry entry1 = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue://{scope}-assign-bugreport-service-queue",
                TestUser.class.getName(), 1, 0);
        EventBusToJmsEntry entry2 = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue://{scope}-query-user-service-queue",
                "query-user-channel", TestUser.class.getName(), 1, 0);
        bridge = EventBusToJmsBridge.run(new Configuration(properties),
                Arrays.asList(entry1, entry2));
        assertNotNull(bridge.getEBListener(entry1));
        assertNotNull(bridge.getJmsListener(entry2));
        bridge.start();
        assertTrue(bridge.isRunning());
        bridge.stop();
        bridge.stop();
        assertFalse(bridge.isRunning());
    }

    @Test
    public void testOnException() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL, "queue://queue",
                "channel", TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        JmsListener listener = bridge.getJmsListener(entry);
        assertFalse(listener.isRunning());
        listener.onException(new JMSException("error"));
        assertTrue(listener.isRunning());
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
        bridge.remove(entry);
        assertNull(bridge.getJmsListener(entry));
    }

    @Test
    public void testOnMessageJmsToEB() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue://query-user-service-queue", "query-user-channel",
                TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        bridge.start();
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder name = new StringBuilder();
        eb.subscribe("query-user-channel", new RequestHandler() {
            @Override
            public void handle(Request<Object> request) {
                try {
                    TestUser u = request.getPayload();
                    name.append(u.name);
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
        Destination dest = jmsContainer
                .getDestination("queue://query-user-service-queue");
        Map<String, Object> headers = new HashMap<>();
        jmsContainer.send(dest, headers, "{\"name\":\"bill\"}");
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals("bill", name.toString());
    }

    @Test
    public void testHandleEBToJms() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create",
                "queue://create", TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        bridge.start();
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "message";
        Request<Object> request = Request
                .objectBuilder()
                .setProtocol(Protocol.HTTP)
                .setMethod(Method.GET)
                .setEndpoint("/w")
                .setProperties(properties)
                .setHeaders(headers)
                .setPayload(payload)
                .setCodecType(CodecType.JSON)
                .setResponse(
                        new Response(new HashMap<String, Object>(),
                                new HashMap<String, Object>(), "",
                                CodecType.JSON))
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder text = new StringBuilder();
        jmsContainer.setMessageListener(
                jmsContainer.getDestination("queue://create"),
                new MessageListener() {
                    @Override
                    public void onMessage(Message message) {
                        try {
                            text.append(((TextMessage) message).getText());
                            latch.countDown();
                        } catch (JMSException e) {
                        }
                    }
                }, new MessageListenerConfig());
        eb.publish("create", request);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals("message", text.toString());
    }

    @Test
    public void testOnMessageJmsToEBWithReplyTo() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL, "queue://myqueue",
                "mychannel", TestUser.class.getName(), 1, 0);
        bridge.add(entry);
        bridge.start();
        final CountDownLatch latch = new CountDownLatch(2);
        final StringBuilder name = new StringBuilder();
        final StringBuilder reply = new StringBuilder();
        eb.subscribe("mychannel", new RequestHandler() {
            @Override
            public void handle(Request<Object> request) {
                TestUser u = request.getPayload();
                name.append(u.name);
                request.getResponse().setPayload("ted");
                // request.getDispatcher().send(request.getResponse());
                latch.countDown();
            }
        }, null);
        Destination dest = jmsContainer.getDestination("queue://myqueue");
        Map<String, Object> headers = new HashMap<>();
        headers.put(Constants.ACCEPT, "application/json");
        jmsContainer.sendReceive(dest, headers, "{\"name\":\"bill\"}",
                new Handler<Response>() {
                    @Override
                    public void handle(Response resp) {
                        reply.append((String) resp.getPayload());
                        latch.countDown();
                    }
                });
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals("ted", reply.toString());
        assertEquals("bill", name.toString());
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
        bridge.remove(entry);
        assertNull(bridge.getEBListener(entry));
    }

    @Test
    public void testLoad() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue://{scope}-assign-bugreport-service-queue",
                TestUser.class.getName(), 1, 0);
        File jsonFile = File.createTempFile("entry", "json");
        FileWriter out = new FileWriter(jsonFile);
        out.write("[" + new JsonObjectCodec().encode(entry) + "]");
        out.close();
        Collection<EventBusToJmsEntry> entries = EventBusToJmsBridge
                .load(jsonFile);
        assertEquals(1, entries.size());
    }
}
