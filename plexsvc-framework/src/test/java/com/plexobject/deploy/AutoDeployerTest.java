package com.plexobject.deploy;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Properties;

import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.jms.JMSTestUtils;
import com.plexobject.jms.impl.JMSUtils;
import com.plexobject.security.AuthException;
import com.plexobject.security.RoleAuthorizer;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceRegistryTest.TestUser;

public class AutoDeployerTest {
    @ServiceConfig(protocol = Protocol.WEBSOCKET, endpoint = "/ws", method = Method.MESSAGE, codec = CodecType.JSON)
    public static class WebsocketService implements RequestHandler {

        @Override
        public void handle(Request request) {
        }
    }

    @ServiceConfig(protocol = Protocol.HTTP, payloadClass = TestUser.class, endpoint = "/w", method = Method.GET, codec = CodecType.JSON, rolesAllowed = "employee")
    public static class WebService implements RequestHandler {
        @Override
        public void handle(Request request) {
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, endpoint = "queue://test", method = Method.MESSAGE, codec = CodecType.JSON)
    public static class JmsService implements RequestHandler {
        @Override
        public void handle(Request request) {
        }
    }

    public static class Authorizer implements RoleAuthorizer {
        @Override
        public void authorize(Request request, String[] roles)
                throws AuthException {
        }
    }

    private static BrokerService broker;
    private static File propFile;
    private static Properties properties = new Properties();

    @BeforeClass
    public static void setUp() throws Exception {
        broker = JMSTestUtils.startBroker(properties);
        properties.put(Constants.HTTP_PORT, "8585");
        properties.put(JMSUtils.JMS_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put(JMSUtils.JMS_CONNECTION_FACTORY_LOOKUP,
                "ConnectionFactory");
        properties.put(JMSUtils.JMS_PROVIDER_URL, "tcp://localhost:61616");
        properties.put(Constants.AUTO_DEPLOY_PACKAGES, "com.plexobject.deploy");
        propFile = File.createTempFile("prop", "config");
        propFile.deleteOnExit();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        broker.stop();
    }

    @Test
    public void testRunWithoutAuthorizer() throws Exception {
        properties.store(new FileWriter(propFile), "");

        AutoDeployer deployer = new AutoDeployer();
        deployer.deploy(propFile.getAbsolutePath());
        Collection<RequestHandler> handlers = deployer.serviceRegistry
                .getHandlers();
        assertEquals(3, handlers.size());
        deployer.serviceRegistry.stop();
    }

    @Test
    public void testRunWithoutAuthorizerAndPing() throws Exception {
        properties.setProperty("enablePingHandlers", "true");
        properties.store(new FileWriter(propFile), "");

        AutoDeployer deployer = new AutoDeployer();
        deployer.deploy(propFile.getAbsolutePath());
        Collection<RequestHandler> handlers = deployer.serviceRegistry
                .getHandlers();
        assertEquals(6, handlers.size()); // double because of ping handlers
        deployer.serviceRegistry.stop();
    }

    @Test
    public void testRunWithAuthorizer() throws Exception {
        properties.put(Constants.PLEXSERVICE_ROLE_AUTHORIZER_CLASS,
                Authorizer.class.getName());
        properties.store(new FileWriter(propFile), "");
        AutoDeployer deployer = new AutoDeployer();
        deployer.deploy(propFile.getAbsolutePath());
        Collection<RequestHandler> handlers = deployer.serviceRegistry
                .getHandlers();
        assertEquals(3, handlers.size());
        deployer.serviceRegistry.stop();
    }

}
