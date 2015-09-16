package com.plexobject.bridge.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
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
import javax.naming.NamingException;

import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSTestUtils;
import com.plexobject.jms.MessageListenerConfig;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;

public class WebToJmsBridgeIntegTest {
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

    private static Object reply;
    private JMSContainer jmsContainer;
    private WebToJmsBridge bridge;
    private static Properties properties = new Properties();
    private static BrokerService broker;
    private ServiceRegistry serviceRegistry;

    @BeforeClass
    public static void setUpClass() throws Exception {
        broker = JMSTestUtils.startBroker(properties);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        broker.stop();
    }

    @Before
    public void setUp() throws Exception {
        jmsContainer = JMSUtils.getJMSContainer(new Configuration(properties));
        Configuration config = new Configuration(new Properties());
        serviceRegistry = new ServiceRegistry(config);
        bridge = new WebToJmsBridge(serviceRegistry, jmsContainer);
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                RequestMethod.GET, "destination", 5, false, 1);
        ServiceConfigDesc desc = new ServiceConfigDesc.Builder(entry).build();
        // add handler for invocation later so that we mimic actual execution
        // path
        serviceRegistry.addRequestHandler(desc, bridge);
    }

    @Test
    public void testConstructor() throws Exception {
        Configuration config = new Configuration(properties);
        ServiceRegistry serviceRegistry = new ServiceRegistry(config);
        JMSContainer jmsContainer = JMSUtils.getJMSContainer(config);
        bridge = new WebToJmsBridge(serviceRegistry, jmsContainer);
    }

    @Test
    public void testSetWebToJmsEntries() throws Exception {
        assertNull(bridge.getMappingEntry(newWebRequest("/ping", "{}")));
        bridge.setWebToJmsEntries(Arrays.asList(new WebToJmsEntry(
                CodecType.JSON, "/ping", RequestMethod.GET, "destination", 5,
                false, 1), new WebToJmsEntry(CodecType.JSON, "/ws",
                RequestMethod.MESSAGE, "destination", 5, false, 1)));
        assertNotNull(bridge.getMappingEntry(newWebRequest("/ping", "{}")));
        assertNotNull(bridge.getMappingEntry(newWebsocketRequest("/ws", "{}")));
    }

    @Test
    public void testAdd() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                RequestMethod.GET, "destination", 5, false, 1);
        bridge.add(entry);
        Request request = newWebRequest("/w", "{}");
        assertNotNull(bridge.getMappingEntry(request));
    }

    @Test
    public void testOnStartedStopped() throws Exception {
        bridge.onCreated();
        assertFalse(jmsContainer.isRunning());
        bridge.onStarted();
        assertTrue(jmsContainer.isRunning());
        bridge.onStopped();
        assertFalse(jmsContainer.isRunning());
        bridge.onDestroyed();
    }

    @Test
    public void testHandleUnknown() throws Exception {
        Request request = newWebRequest("/w", "message");
        serviceRegistry.invoke(request, bridge);
        assertTrue(reply.toString().contains("Unknown request received"));
    }

    @Test
    public void testHandleAsynchronous() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                RequestMethod.GET, "queue://create", 5, true, 1);
        bridge.add(entry);
        bridge.onStarted();
        Request request = newWebRequest("/w", "message");
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder text = new StringBuilder();
        final Destination dest = jmsContainer.getDestination("queue://create");
        jmsContainer.setMessageListener(dest, new MessageListener() {
            @Override
            public void onMessage(Message m) {
                try {
                    TextMessage tm = (TextMessage) m;
                    text.append(tm.getText());
                    latch.countDown();
                } catch (JMSException e) {
                }
            }
        }, new MessageListenerConfig());
        bridge.handle(request);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals("message", text.toString());
    }

    @Test
    public void testHandleSynchronous() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/ws",
                RequestMethod.MESSAGE, "queue://svc", 5, false, 1);
        bridge.add(entry);
        bridge.onStarted();
        Request request = newWebsocketRequest("/ws", "message");
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder text = new StringBuilder();
        final Destination dest = jmsContainer.getDestination("queue://svc");
        jmsContainer.setMessageListener(dest, new MessageListener() {
            @Override
            public void onMessage(Message m) {
                try {
                    TextMessage tm = (TextMessage) m;
                    jmsContainer.send(tm.getJMSReplyTo(),
                            new HashMap<String, Object>(), "my-reply");
                    text.append(tm.getText());
                    latch.countDown();
                } catch (JMSException e) {
                } catch (NamingException e) {
                }
            }
        }, new MessageListenerConfig());
        bridge.handle(request);
        latch.await(1000, TimeUnit.MILLISECONDS);
        assertEquals("message", text.toString());
    }

    @Test
    public void testHandleSynchronousTimeout() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/ws",
                RequestMethod.MESSAGE, "queue://tm", 1, false, 1);
        bridge.add(entry);
        bridge.onStarted();
        Request request = newWebsocketRequest("/ws", "message");
        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder text = new StringBuilder();
        final Destination dest = jmsContainer.getDestination("queue://tm");
        jmsContainer.setMessageListener(dest, new MessageListener() {
            @Override
            public void onMessage(Message m) {
                try {
                    TextMessage tm = (TextMessage) m;
                    text.append(tm.getText());
                    latch.countDown();
                } catch (JMSException e) {
                }
            }
        }, new MessageListenerConfig());
        serviceRegistry.invoke(request, bridge);
        latch.await(2000, TimeUnit.MILLISECONDS);
        assertEquals("message", text.toString());
        assertTrue(reply.toString(),
                reply.toString().contains("Request timedout"));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddDuplicateWeb() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/ws",
                RequestMethod.GET, "destination", 5, false, 1);
        bridge.add(entry);
        bridge.add(entry);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddDuplicateWebsocket() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/ws",
                RequestMethod.MESSAGE, "destination", 5, false, 1);
        bridge.add(entry);
        bridge.add(entry);
    }

    @Test
    public void testEquals() throws Exception {
        assertTrue(bridge.equals(bridge));
        assertFalse(bridge.equals(null));
    }

    private static Request newWebRequest(String path, String payload) {
        return newRequest(path, payload, RequestMethod.GET);
    }

    private static Request newWebsocketRequest(String path, String payload) {
        return newRequest(path, payload, RequestMethod.MESSAGE);
    }

    private static Request newRequest(String path, String payload,
            RequestMethod method) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("prop1", "val1");
        Map<String, Object> headers = new HashMap<>();
        headers.put("head1", "val1");
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(method).setEndpoint(path).setProperties(properties)
                .setHeaders(headers).setContents(payload)
                .setCodecType(CodecType.JSON)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                    @Override
                    protected void doSend(Response response,
                            Object encodedPayload) {
                        reply = response.getContents();
                    }
                }).build();
        return request;
    }

    @Test
    public void testLoad() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/ws",
                RequestMethod.MESSAGE, "destination", 5, false, 1);
        File jsonFile = File.createTempFile("entry", "json");
        FileWriter out = new FileWriter(jsonFile);
        String jsonStr = "[" + new JsonObjectCodec().encode(entry) + "]";
        out.write(jsonStr);
        out.close();
        Collection<WebToJmsEntry> entries = WebToJmsBridge
                .fromJSONFile(jsonFile);
        assertEquals(1, entries.size());
    }
}
