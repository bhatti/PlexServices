package com.plexobject.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.service.ServiceRegistryTest.TestUser;

public class ServiceHandlerLifecycleTest {
    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = TestUser.class, endpoint = "/w", method = Method.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public class WebService implements RequestHandler {
        @Override
        public void handle(Request<Object> request) {
        }
    }

    public class PingableWebService extends WebService implements Pingable {
        @Override
        public String ping() {
            return "success";
        }
    }

    private ServiceRegistry registry;
    private BrokerService broker = new BrokerService();

    @Before
    public void setUp() throws Exception {
        final Properties properties = new Properties();
        broker.addConnector("tcp://localhost:61616");
        broker.start();

        properties.put(JMSUtils.JMS_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put(JMSUtils.JMS_CONNECTION_FACTORY_LOOKUP,
                "ConnectionFactory");
        properties.put(JMSUtils.JMS_PROVIDER_URL, "tcp://localhost:61616");
        final Configuration config = new Configuration(properties);

        registry = new ServiceRegistry(config);
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
        assertTrue(handlerLifecycle.ping().contains("WebService"));
    }

    @Test
    public void testPing() throws Exception {
        final PingableWebService handler = new PingableWebService();
        ServiceHandlerLifecycle handlerLifecycle = new ServiceHandlerLifecycle(
                registry, handler);
        handlerLifecycle.start();
        assertEquals("success", handlerLifecycle.ping());
    }

    @Test
    public void testGetSummary() throws Exception {
        final PingableWebService handler = new PingableWebService();
        ServiceHandlerLifecycle handlerLifecycle = new ServiceHandlerLifecycle(
                registry, handler);
        handlerLifecycle.start();
        assertTrue(handlerLifecycle.getSummary().contains("percentile"));
    }
}
