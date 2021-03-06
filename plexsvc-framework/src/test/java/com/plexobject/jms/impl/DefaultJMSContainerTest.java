package com.plexobject.jms.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.apache.activemq.broker.BrokerService;
import org.junit.Test;

import com.plexobject.domain.Configuration;
import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSTestUtils;

public class DefaultJMSContainerTest {
    private final Properties properties = new Properties();
    private List<Message> messages = new ArrayList<>();

    class JmsListener implements MessageListener {
        JMSContainer client;

        JmsListener(JMSContainer client) {
            this.client = client;
        }

        @Override
        public void onMessage(final Message message) {
            messages.add(message);
            try {
                if (message.getJMSReplyTo() != null) {
                    client.send(message.getJMSReplyTo(),
                            new HashMap<String, Object>(),
                            ((TextMessage) message).getText());
                }
            } catch (JMSException e) {
                e.printStackTrace();
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithoutContextFactory() throws Exception {
        final Configuration config = new Configuration(properties);
        JMSUtils.getJMSContainer(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithouFactoryLookup() throws Exception {
        properties.put(JMSUtils.JMS_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        final Configuration config = new Configuration(properties);
        JMSUtils.getJMSContainer(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateWithoutProviderUrl() throws Exception {
        properties.put(JMSUtils.JMS_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put(JMSUtils.JMS_CONNECTION_FACTORY_LOOKUP,
                "ConnectionFactory");
        final Configuration config = new Configuration(properties);
        JMSUtils.getJMSContainer(config);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithBadFactoryLookup() throws Exception {
        properties.put(JMSUtils.JMS_CONTEXT_FACTORY, "xxxx");
        properties.put(JMSUtils.JMS_CONNECTION_FACTORY_LOOKUP,
                "ConnectionFactory");
        properties.put(JMSUtils.JMS_PROVIDER_URL, "tcp://localhost:61619");
        final Configuration config = new Configuration(properties);
        JMSUtils.getJMSContainer(config);
    }

    @Test
    public void testCreateWithServer() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        JMSContainer client = JMSUtils.getJMSContainer(config);
        broker.stop();
        assertFalse(client.isRunning());
    }

    @Test
    public void testCreateWithServerWithUsername() throws Exception {
        properties.put("jms.username", "");
        properties.put("jms.password", "");
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        JMSContainer client = JMSUtils.getJMSContainer(config);
        broker.stop();
        assertFalse(client.isRunning());
    }

    @Test
    public void testStart() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        JMSContainer client = JMSUtils.getJMSContainer(config);
        client.start();
        client.start();
        assertTrue(client.isRunning());
        broker.stop();
    }

    @Test(expected = RuntimeException.class)
    public void testStartWhenBrokerIsDown() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        JMSContainer client = JMSUtils.getJMSContainer(config);
        broker.stop();
        client.start();
    }

    @Test
    public void testStop() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        JMSContainer client = JMSUtils.getJMSContainer(config);
        client.stop();
        assertFalse(client.isRunning());
        client.start();
        assertTrue(client.isRunning());
        client.stop();
        assertFalse(client.isRunning());
        broker.stop();
    }

    @Test
    public void testStopWhenBrokerIsDown() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        JMSContainer client = JMSUtils.getJMSContainer(config);
        client.start();
        broker.stop();
        client.stop(); // client won't throw any exception
    }

    @Test
    public void testCreateProducer() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        DefaultJMSContainer client = new DefaultJMSContainer(config);
        MessageProducer producer = client.createProducer("topic://test");
        assertNotNull(producer);
        MessageProducer producerCopy = client.createProducer("topic://test");
        assertTrue(producer == producerCopy); // should be same reference
        broker.stop();
    }

    @Test
    public void testCreatePersistentProducer() throws Exception {
        properties.put("jms.test.ttl", "1");
        properties.put("jms.test.persistent", "true");
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        DefaultJMSContainer client = new DefaultJMSContainer(config);
        MessageProducer producer = client.createProducer("queue://test");
        assertNotNull(producer);
        MessageProducer producerCopy = client.createProducer("queue://test");
        assertTrue(producer == producerCopy); // should be same reference
        broker.stop();
    }

    @Test
    public void testCreateTemporaryProducer() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        DefaultJMSContainer client = new DefaultJMSContainer(config);
        TemporaryQueue q = client.createTemporaryQueue();
        MessageProducer producer = client.createProducer(q);
        assertNotNull(producer);
        broker.stop();
    }

    @Test
    public void testSend() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        DefaultJMSContainer client = new DefaultJMSContainer(config);
        JmsListener listener = new JmsListener(client);
        client.createConsumer("queue://test").setMessageListener(listener);
        client.start();
        Map<String, Object> headers = new HashMap<>();
        headers.put("key1", "value1");
        headers.put("key2", 2);
        headers.put("key3", 3L);
        headers.put("key4", 4.0F);
        headers.put("key5", 5.0);
        headers.put("key6", (short) 6);
        headers.put("key7", true);
        headers.put("key8", (byte) 8);
        headers.put("key9", new Date());
        client.send(client.getDestination("queue://test"), headers, "payload");
        Thread.sleep(1000);
        broker.stop();
        assertEquals(1, messages.size());
        assertEquals("payload", ((TextMessage) messages.get(0)).getText());
    }

    @Test
    public void testSendReceive() throws Exception {
        BrokerService broker = JMSTestUtils.startBroker(properties);
        final Configuration config = new Configuration(properties);
        DefaultJMSContainer client = new DefaultJMSContainer(config);
        JmsListener listener = new JmsListener(client);
        client.createConsumer("queue://test").setMessageListener(listener);
        client.start();
        Map<String, Object> headers = new HashMap<>();
        headers.put("key1", "value1");
        headers.put("key2", 2);
        headers.put("key3", 3L);
        headers.put("key4", 4.0F);
        headers.put("key5", 5.0);
        headers.put("key6", (short) 6);
        headers.put("key7", true);
        headers.put("key8", (byte) 8);
        headers.put("key9", new Date());
        final StringBuilder payload = new StringBuilder();
        client.sendReceive(client.getDestination("queue://test"), headers,
                "ping", new Handler<Response>() {
                    @Override
                    public void handle(Response request) {
                        payload.append((String) request.getContentsAs());
                    }
                });
        Thread.sleep(1000);
        broker.stop();
        assertEquals("ping", payload.toString());
    }

}
