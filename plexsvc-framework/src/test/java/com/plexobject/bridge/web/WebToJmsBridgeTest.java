package com.plexobject.bridge.web;

import static org.junit.Assert.assertNotNull;

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

import com.plexobject.bus.EventBus;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.jms.JmsClient;
import com.plexobject.service.Lifecycle;
import com.plexobject.service.ServiceConfig.Method;

@RunWith(JMockit.class)
public class WebToJmsBridgeTest {
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
    @Mocked
    private Lifecycle server;

    private WebToJmsBridge bridge;

    @Before
    public void setUp() throws Exception {
        bridge = new WebToJmsBridge(jmsClient, new ArrayList<WebToJmsEntry>());
        bridge.setServer(server);
    }

    @Test
    public void testAdd() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                Method.GET, "destination", 5);
        bridge.add(entry);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

        Request request = new Request(Method.GET, "/w", properties, headers,
                payload, new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });
        assertNotNull(bridge.getMappingEntry(request));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleAsynchronous() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                Method.GET, "queue:{scope}-assign-bugreport-service-queue", 5);
        entry.setAsynchronous(true);
        bridge.add(entry);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

        Request request = new Request(Method.GET, "/w", properties, headers,
                payload, new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });

        new Expectations() {
            {
                jmsClient.send("queue:{scope}-assign-bugreport-service-queue",
                        (Map<String, Object>) any, anyString);
            }
        };
        bridge.handle(request);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleSynchronous() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                Method.GET, "queue:{scope}-assign-bugreport-service-queue", 5);
        entry.setAsynchronous(false);
        bridge.add(entry);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

        Request request = new Request(Method.GET, "/w", properties, headers,
                payload, new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });

        new Expectations() {
            {
                jmsClient.sendReceive(entry.getDestination(),
                        (Map<String, Object>) any,
                        (String) request.getPayload(), (Handler<Response>) any,
                        true);
            }
        };
        bridge.handle(request);
    }
}
