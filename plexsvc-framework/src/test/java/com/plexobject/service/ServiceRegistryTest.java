package com.plexobject.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.bridge.web.WebToJmsEntry;
import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.Response;
import com.plexobject.jms.JMSTestUtils;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.security.AuthException;
import com.plexobject.security.SecurityAuthorizer;
import com.plexobject.validation.ValidationException;

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

    private SecurityAuthorizer authorizer = new SecurityAuthorizer() {
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
    @ServiceConfig(protocol = Protocol.WEBSOCKET, endpoint = "/ws", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    public class WebsocketService implements RequestHandler {
        @Override
        public void handle(Request request) {
            requests.add(request);
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = TestUser.class, endpoint = "/w", method = RequestMethod.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request request) {
            requests.add(request);
            request.getResponse().setPayload(request.getPayload());
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, endpoint = "queue://test", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    public class JmsService implements RequestHandler {
        @Override
        public void handle(Request request) {
            requests.add(request);
        }
    }

    private static BrokerService broker;

    @BeforeClass
    public static void setUp() throws Exception {
        broker = JMSTestUtils.startBroker(new Properties());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        broker.stop();
    }

    @Test
    public void testCreateWithoutEmptyServices() throws Exception {
        final Configuration config = new Configuration(properties);

        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        assertEquals(config, registry.getConfiguration());

        assertEquals(0, registry.getHandlers().size());
        assertNotNull(registry.getServiceMetricsRegistry());
    }

    @Test
    public void testCreateServices() throws Exception {
        final Configuration config = initProperties();

        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        registry.add(new WebsocketService());
        registry.add(new WebService());
        registry.add(new JmsService());
        assertEquals(3, registry.getHandlers().size());
        assertNotNull(registry.getServiceMetricsRegistry());
        assertEquals(3, registry.getServiceConfigurations().size());
        assertTrue(registry.dumpServiceConfigurations().contains("HTTP:GET"));
    }

    private Configuration initProperties() {
        properties.put(Constants.HTTP_PORT, 8282);
        properties.put("statsd.host", "localhost");
        properties.put(JMSUtils.JMS_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put(JMSUtils.JMS_CONNECTION_FACTORY_LOOKUP,
                "ConnectionFactory");
        properties.put(JMSUtils.JMS_PROVIDER_URL, "tcp://localhost:61616");

        final Configuration config = new Configuration(properties);
        return config;
    }

    @Test
    public void testSetRequestHandlers() throws Exception {
        final Configuration config = initProperties();

        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        registry.setRequestHandlers(Arrays.asList(new WebsocketService(),
                new WebService(), new JmsService()));
        assertEquals(3, registry.getHandlers().size());
        assertNotNull(registry.getServiceMetricsRegistry());
    }

    @Test
    public void testAddRemoveInterceptor() throws Exception {
        final Configuration config = new Configuration(properties);
        Interceptor<Request> interceptor1 = new Interceptor<Request>() {
            @Override
            public Request intercept(Request request) {
                return request;
            }
        };
        Interceptor<Request> interceptor2 = new Interceptor<Request>() {
            @Override
            public Request intercept(Request request) {
                return request;
            }
        };

        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        RequestHandler h = new WebService();
        registry.add(h);
        registry.addRequestInterceptor(interceptor1);
        registry.addRequestInterceptor(interceptor2);

        assertEquals(2, registry.getRequestInterceptors().size());
        assertTrue(registry.removeRequestInterceptor(interceptor2));
        assertEquals(1, registry.getRequestInterceptors().size());
    }

    @Test
    public void testAddRemoveService() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
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
    public void testAddWebToJmsEntriesWithPings() throws Exception {
        properties.setProperty("enablePingHandlers", "true");
        final Configuration config = initProperties();
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Collection<WebToJmsEntry> entries = Arrays.asList(new WebToJmsEntry(
                CodecType.JSON, "/w", RequestMethod.GET, "queue://w", 5, false,
                1), new WebToJmsEntry(CodecType.JSON, "/ws", RequestMethod.GET,
                "queue://w", 5, false, 1));
        RequestHandler h = new WebService();
        registry.add(h);
        registry.setWebToJmsEntries(entries);
        assertEquals(4, registry.getHandlers().size()); // double because of
                                                        // ping handlers
    }

    @Test
    public void testAddWebToJmsEntries() throws Exception {
        final Configuration config = initProperties();
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Collection<WebToJmsEntry> entries = Arrays.asList(new WebToJmsEntry(
                CodecType.JSON, "/w", RequestMethod.GET, "queue://w", 5, false,
                1), new WebToJmsEntry(CodecType.JSON, "/ws", RequestMethod.GET,
                "queue://w", 5, false, 1));
        RequestHandler h = new WebService();
        registry.add(h);
        registry.setWebToJmsEntries(entries);
        assertEquals(2, registry.getHandlers().size());
    }

    @Test
    public void testUnknownRemove() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        RequestHandler h = new WebService();
        registry.remove(h);
        assertEquals(0, registry.getHandlers().size());
    }

    @Test
    public void testStartStop() throws Exception {
        properties.put("statsd.host", "localhost");
        properties.put(Constants.HTTP_PORT, "8282");

        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        RequestHandler h = new WebService();
        registry.add(h);
        assertFalse(registry.isRunning());
        registry.start();
        assertTrue(registry.isRunning());
        registry.stop();
        assertFalse(registry.isRunning());
    }

    @Test
    public void testStartStopWithServiceRegistryLifecycleAware()
            throws Exception {
        properties.put("statsd.host", "localhost");
        properties.put(Constants.HTTP_PORT, "8282");

        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        registry.addServiceRegistryLifecycleAware(new ServiceRegistryLifecycleAware() {
            @Override
            public void onStarted(ServiceRegistry serviceRegistry) {
            }

            @Override
            public void onStopped(ServiceRegistry serviceRegistry) {
            }
        });

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
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";

        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(headers)
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();

        registry.invoke(request, null);
        assertEquals(payload, request.getPayload());
    }

    @Test
    public void testInvokeWebsocket() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "test";

        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(headers)
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        RequestHandler h = new WebsocketService();
        registry.invoke(request, h);
        assertEquals("test", request.getPayload());
    }

    @Test
    public void testInvokeWeb() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{\"username\":\"john\"}";
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(headers)
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        RequestHandler h = new WebService();
        registry.invoke(request, h);
        TestUser user1 = request.getPayload();
        assertEquals("john", user1.getUsername());
        TestUser user2 = request.getResponse().getPayload();
        assertEquals("john", user2.getUsername());
    }

    @Test
    public void testInvokeWebWithAuthExceptionAndRedirect() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder out = new StringBuilder();
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(headers)
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                    @Override
                    protected void doSend(Response r, String text) {
                        out.append(text);
                    }
                }).build();
        RequestHandler h = new WebService();
        authException = new AuthException("authCode", "bad auth");
        registry.invoke(request, h);
        assertTrue(out.toString().contains("AuthException"));
    }

    @Test
    public void testInvokeWebWithAuthException() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder out = new StringBuilder();
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(headers)
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                    @Override
                    protected void doSend(Response r, String text) {
                        out.append(text);
                    }
                }).build();

        RequestHandler h = new WebService();
        authException = new AuthException("authCode", "bad auth");
        registry.invoke(request, h);
        assertTrue("Out: " + out, out.toString().contains("AuthException"));
    }

    @Test
    public void testInvokeWebWithValidationException() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder out = new StringBuilder();
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(headers)
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                    @Override
                    protected void doSend(Response r, String text) {
                        out.append(text);
                    }
                }).build();
        RequestHandler h = new WebService();
        valException = (ValidationException) ValidationException.builder()
                .addError("code", "field", "msg").build();
        registry.invoke(request, h);
        assertTrue(out.toString().contains("ValidationException"));
    }

    @Test
    public void testInvokeWebWithException() throws Exception {
        final Configuration config = new Configuration(properties);
        ServiceRegistry registry = new ServiceRegistry(config);
        registry.setSecurityAuthorizer(authorizer);
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        String payload = "{}";
        final StringBuilder out = new StringBuilder();
        Request request = Request.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(headers)
                .setCodecType(CodecType.JSON).setPayload(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                    @Override
                    protected void doSend(Response r, String text) {
                        out.append(text);
                    }
                }).build();
        RequestHandler h = new WebService();
        exception = new RuntimeException("unknown error");
        registry.invoke(request, h);
        assertTrue(out.toString().contains("unknown error"));
    }

}
