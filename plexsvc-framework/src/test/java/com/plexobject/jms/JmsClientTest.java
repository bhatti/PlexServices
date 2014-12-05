package com.plexobject.jms;

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

import com.plexobject.handler.Handler;
import com.plexobject.handler.Response;
import com.plexobject.util.Configuration;

public class JmsClientTest {
    private final Properties properties = new Properties();
    private List<Message> messages = new ArrayList<>();

    class JmsListener implements MessageListener {
        JmsClient client;

        JmsListener(JmsClient client) {
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

    @Test(expected = NullPointerException.class)
    public void testCreateWithoutContextFactory() throws Exception {
        final Configuration config = new Configuration(properties);
        new JmsClient(config);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateWithouFactoryLookup() throws Exception {
        properties.put("jms.contextFactory",
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        final Configuration config = new Configuration(properties);
        new JmsClient(config);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateWithoutProviderUrl() throws Exception {
        properties.put("jms.contextFactory",
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put("jms.connectionFactoryLookup", "ConnectionFactory");
        final Configuration config = new Configuration(properties);
        new JmsClient(config);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateWithBadFactoryLookup() throws Exception {
        properties.put("jms.contextFactory", "xxxx");
        properties.put("jms.connectionFactoryLookup", "ConnectionFactory");
        properties.put("jms.providerUrl", "tcp://localhost:61616");
        final Configuration config = new Configuration(properties);
        new JmsClient(config);
    }

    @Test
    public void testCreateWithServer() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        broker.stop();
        assertFalse(client.isRunning());
    }

    @Test
    public void testCreateWithServerWithUsername() throws Exception {
        properties.put("jms.username", "");
        properties.put("jms.password", "");
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        broker.stop();
        assertFalse(client.isRunning());
    }

    @Test
    public void testStart() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        client.start();
        client.start();
        assertTrue(client.isRunning());
        broker.stop();
    }

    @Test(expected = RuntimeException.class)
    public void testStartWhenBrokerIsDown() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        broker.stop();
        client.start();
    }

    @Test
    public void testStop() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        client.stop();
        assertFalse(client.isRunning());
        client.start();
        assertTrue(client.isRunning());
        client.stop();
        assertFalse(client.isRunning());
        broker.stop();
    }

    @Test(expected = RuntimeException.class)
    public void testStopWhenBrokerIsDown() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        client.start();
        broker.stop();
        client.stop();
    }

    @Test
    public void testCreateProducer() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        MessageProducer producer = client.createProducer("topic:test");
        assertNotNull(producer);
        MessageProducer producerCopy = client.createProducer("topic:test");
        assertTrue(producer == producerCopy); // should be same reference
        broker.stop();
    }

    @Test
    public void testCreatePersistentProducer() throws Exception {
        properties.put("jms.test.ttl", "1");
        properties.put("jms.test.persistent", "true");
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        MessageProducer producer = client.createProducer("queue:test");
        assertNotNull(producer);
        MessageProducer producerCopy = client.createProducer("queue:test");
        assertTrue(producer == producerCopy); // should be same reference
        broker.stop();
    }

    @Test
    public void testCreateTemporaryProducer() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        TemporaryQueue q = client.createTemporaryQueue();
        MessageProducer producer = client.createProducer(q);
        assertNotNull(producer);
        broker.stop();
    }

    @Test
    public void testSend() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        JmsListener listener = new JmsListener(client);
        client.createConsumer("queue:test").setMessageListener(listener);
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
        client.send("queue:test", headers, "payload");
        Thread.sleep(1000);
        broker.stop();
        assertEquals(1, messages.size());
        assertEquals("payload", ((TextMessage) messages.get(0)).getText());
    }

    @Test
    public void testSendReceive() throws Exception {
        BrokerService broker = startBroker();
        final Configuration config = new Configuration(properties);
        JmsClient client = new JmsClient(config);
        JmsListener listener = new JmsListener(client);
        client.createConsumer("queue:test").setMessageListener(listener);
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
        client.sendReceive("queue:test", headers, "ping",
                new Handler<Response>() {
                    @Override
                    public void handle(Response request) {
                        payload.append((String) request.getPayload());
                    }
                });
        Thread.sleep(1000);
        broker.stop();
        assertEquals("ping", payload.toString());
    }

    private BrokerService startBroker() throws Exception {
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
