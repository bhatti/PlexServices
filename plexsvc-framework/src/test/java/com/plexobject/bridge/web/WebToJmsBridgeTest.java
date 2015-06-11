package com.plexobject.bridge.web;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.plexobject.bus.EventBus;
import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.jms.JMSContainer;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceRegistry;

@RunWith(JMockit.class)
public class WebToJmsBridgeTest {
    public static class TestUser {

    }

    @Mocked
    private JMSContainer jmsContainer;
    @Mocked
    private EventBus eb;
    @Mocked
    private MessageConsumer consumer;
    @Mocked
    private TextMessage message;

    private WebToJmsBridge bridge;

    @Before
    public void setUp() throws Exception {
        Configuration config = new Configuration(new Properties());
        ServiceRegistry serviceRegistry = new ServiceRegistry(config, null);
        bridge = new WebToJmsBridge(serviceRegistry, jmsContainer);
    }

    @Test
    public void testAdd() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                Method.GET, "destination", 5, false, 1);
        bridge.add(entry);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

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

        assertNotNull(bridge.getMappingEntry(request));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleAsynchronous() throws Exception {
        WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                Method.GET, "queue://{scope}-assign-bugreport-service-queue",
                5, false, 1);
        entry.setAsynchronous(true);
        bridge.add(entry);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

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

        new Expectations() {
            {
                Destination dest = jmsContainer
                        .getDestination("queue://{scope}-assign-bugreport-service-queue");
                jmsContainer.send(dest, (Map<String, Object>) any, anyString);
            }
        };
        bridge.handle(request);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHandleSynchronous() throws Exception {
        final WebToJmsEntry entry = new WebToJmsEntry(CodecType.JSON, "/w",
                Method.GET, "queue://{scope}-assign-bugreport-service-queue",
                5, false, 1);
        entry.setAsynchronous(false);
        bridge.add(entry);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

        final Request<Object> request = Request
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

        new Expectations() {
            {
                Destination dest = jmsContainer.getDestination(entry
                        .getDestination());
                jmsContainer.sendReceive(dest, (Map<String, Object>) any,
                        (String) request.getPayload(), (Handler<Response>) any);
            }
        };
        bridge.handle(request);
    }
}
