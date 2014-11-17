package com.plexobject.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.JmsClient;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceRegistryTest.TestUser;
import com.plexobject.util.Configuration;

public class ServiceHandlerLifecycleTest {
    @ServiceConfig(protocol = Protocol.HTTP, requestClass = TestUser.class, endpoint = "/w", method = Method.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request request) {
        }
    }

    public class PingableWebService extends WebService implements Pingable {
        @Override
        public int ping() {
            return 200;
        }
    }

    private ServiceRegistry registry;
    private JmsClient jmsClient;
    private BrokerService broker = new BrokerService();

    @Before
    public void setUp() throws Exception {
        final Properties properties = new Properties();
        broker.addConnector("tcp://localhost:61616");
        broker.start();

        properties.put("jms.contextFactory",
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put("jms.connectionFactoryLookup", "ConnectionFactory");
        properties.put("jms.providerUrl", "tcp://localhost:61616");
        final Configuration config = new Configuration(properties);

        jmsClient = new JmsClient(config);
        registry = new ServiceRegistry(config, null, jmsClient);
    }

    @After
    public void tearDown() throws Exception {
        broker.stop();
    }

    @Test
    public void testStartStop() throws Exception {
        final WebService handler = new WebService();
        ServiceHandlerLifecycle handlerLifecycle = new ServiceHandlerLifecycle(
                registry, handler);
        assertEquals(0, registry.getHandlers().size());
        handlerLifecycle.start();
        assertEquals("handlers " + registry.getHandlers(), 1, registry
                .getHandlers().size());
        assertTrue(handlerLifecycle.isRunning());
        handlerLifecycle.stop();
        assertEquals(0, registry.getHandlers().size());
        assertFalse(registry.isRunning());
        assertEquals(-1, handlerLifecycle.ping());
    }

    @Test
    public void testPing() throws Exception {
        final PingableWebService handler = new PingableWebService();
        ServiceHandlerLifecycle handlerLifecycle = new ServiceHandlerLifecycle(
                registry, handler);
        handlerLifecycle.start();
        assertEquals(200, handlerLifecycle.ping());
    }

    @Test
    public void testGetSummary() throws Exception {
        final PingableWebService handler = new PingableWebService();
        ServiceHandlerLifecycle handlerLifecycle = new ServiceHandlerLifecycle(
                registry, handler);
        handlerLifecycle.start();
        assertTrue(handlerLifecycle.getSummary().contains("Summary"));
    }
}
