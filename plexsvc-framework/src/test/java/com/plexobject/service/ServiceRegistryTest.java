package com.plexobject.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.JmsClient;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.util.Configuration;

public class ServiceRegistryTest {
    public static class TestUser {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    private RoleAuthorizer authorizer = new RoleAuthorizer() {
        @Override
        public void authorize(Request request, String[] roles)
                throws AuthException {
            if (authException != null) {
                throw authException;
            }
            if (valException != null) {
                throw valException;
            }
            if (exception != null) {
                throw exception;
            }
        }
    };
    private AuthException authException;
    private ValidationException valException;
    private RuntimeException exception;
    private final Properties properties = new Properties();
    private List<Request> requests = new ArrayList<>();

    //
    @ServiceConfig(protocol = Protocol.WEBSOCKET, endpoint = "/ws", method = Method.MESSAGE, codec = CodecType.JSON)
    public class WebsocketService implements RequestHandler {

        @Override
        public void handle(Request request) {
            requests.add(request);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = TestUser.class, endpoint = "/w", method = Method.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request request) {
            requests.add(request);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, endpoint = "queue:test", method = Method.MESSAGE, codec = CodecType.JSON)
    public class JmsService implements RequestHandler {
        @Override
        public void handle(Request request) {
            requests.add(request);
        }
    }

    private static BrokerService broker;

    @BeforeClass
    public static void setUp() throws Exception {
        broker = startBroker();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        broker.stop();
    }

    @Test
    public void testCreateWithoutEmptyServices() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();

        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        assertEquals(0, registry.getHandlers().size());
        assertNotNull(registry.getServiceMetricsRegistry());
    }

    @Test
    public void testCreateServices() throws Exception {
        properties.put("statsd.host", "localhost");
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();

        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        registry.add(new WebsocketService());
        registry.add(new WebService());
        registry.add(new JmsService());
        assertEquals(3, registry.getHandlers().size());
        assertNotNull(registry.getServiceMetricsRegistry());
    }

    @Test
    public void testAddRemoveService() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        RequestHandler h = new WebService();
        assertFalse(registry.exists(h));
        registry.add(h);
        assertTrue(registry.exists(h));
        registry.add(h);
        assertEquals(1, registry.getHandlers().size());
        registry.remove(h);
        assertEquals(0, registry.getHandlers().size());
        registry.remove(h);
        assertEquals(0, registry.getHandlers().size());
        assertFalse(registry.exists(h));
    }

    @Test
    public void testUnknownRemove() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        RequestHandler h = new WebService();
        registry.remove(h);
        assertEquals(0, registry.getHandlers().size());
    }

    @Test
    public void testStartStop() throws Exception {
        properties.put("statsd.host", "localhost");
        final JmsClient jmsClient = newJmsClient();
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        RequestHandler h = new WebService();
        registry.add(h);
        assertFalse(registry.isRunning());
        registry.start();
        assertTrue(registry.isRunning());
        registry.stop();
        assertFalse(registry.isRunning());
    }

    @Test
    public void testInvokeNull() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {

                    }
                });
        registry.invoke(request, null);
        assertEquals(payload, request.getPayload());
    }

    @Test
    public void testInvokeWebsocket() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "test";

        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {
                    }
                });
        RequestHandler h = new WebsocketService();
        registry.invoke(request, h);
        assertEquals("test", request.getPayload());
    }

    @Test
    public void testInvokeWeb() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{\"username\":\"john\"}";

        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    @Override
                    public void addSessionId(String value) {
                    }
                });
        RequestHandler h = new WebService();
        registry.invoke(request, h);
        TestUser user = request.getPayload();
        assertEquals("john", user.getUsername());
    }

    @Test
    public void testInvokeWebWithAuthExceptionAndRedirect() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder response = new StringBuilder();
        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    {
                        setCodecType(CodecType.TEXT);
                    }

                    @Override
                    public void addSessionId(String value) {
                    }

                    @Override
                    protected void doSend(String payload) {
                        response.append(payload);
                    }
                });
        RequestHandler h = new WebService();
        authException = new AuthException(401, "sessionId", "location",
                "bad auth");
        registry.invoke(request, h);
        assertTrue(response.toString().contains("AuthException"));
    }

    @Test
    public void testInvokeWebWithAuthException() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder response = new StringBuilder();
        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    {
                        setCodecType(CodecType.TEXT);
                    }

                    @Override
                    public void addSessionId(String value) {
                    }

                    @Override
                    protected void doSend(String payload) {
                        response.append(payload);
                    }
                });
        RequestHandler h = new WebService();
        authException = new AuthException(401, "sessionId", "bad auth");
        registry.invoke(request, h);
        assertTrue(response.toString().contains("AuthException"));
    }

    @Test
    public void testInvokeWebWithValidationException() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder response = new StringBuilder();
        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    {
                        setCodecType(CodecType.TEXT);
                    }

                    @Override
                    public void addSessionId(String value) {
                    }

                    @Override
                    protected void doSend(String payload) {
                        response.append(payload);
                    }
                });
        RequestHandler h = new WebService();
        valException = ValidationException.builder()
                .addError("code", "field", "msg").build();
        registry.invoke(request, h);
        assertTrue(response.toString().contains("ValidationException"));
    }

    @Test
    public void testInvokeWebWithException() throws Exception {
        final Configuration config = new Configuration(properties);
        final JmsClient jmsClient = newJmsClient();
        ServiceRegistry registry = new ServiceRegistry(config, authorizer,
                jmsClient);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder response = new StringBuilder();
        Request request = new Request(Protocol.HTTP, Method.GET, "/w",
                properties, headers, payload, new AbstractResponseDispatcher() {
                    {
                        setCodecType(CodecType.TEXT);
                    }

                    @Override
                    public void addSessionId(String value) {
                    }

                    @Override
                    protected void doSend(String payload) {
                        response.append(payload);
                    }
                });
        RequestHandler h = new WebService();
        exception = new RuntimeException("unknown error");
        registry.invoke(request, h);
        assertTrue(response.toString().contains("unknown error"));
    }

    private JmsClient newJmsClient() throws Exception {
        properties.put("jms.contextFactory",
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put("jms.connectionFactoryLookup", "ConnectionFactory");
        properties.put("jms.providerUrl", "tcp://localhost:61616");
        final Configuration config = new Configuration(properties);
        return new JmsClient(config);
    }

    private static BrokerService startBroker() throws Exception {
        Properties properties = new Properties();
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.start();

        properties.put("jms.contextFactory",
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put("jms.connectionFactoryLookup", "ConnectionFactory");
        properties.put("jms.providerUrl", "tcp://localhost:61616");
        return broker;
    }
}
