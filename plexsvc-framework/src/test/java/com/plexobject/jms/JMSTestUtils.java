package com.plexobject.jms;

import java.util.Properties;

import org.apache.activemq.broker.BrokerService;

import com.plexobject.jms.impl.JMSUtils;

public class JMSTestUtils {
    public static BrokerService startBroker(Properties properties)
            throws Exception {
        properties.put(JMSUtils.JMS_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        properties.put(JMSUtils.JMS_CONNECTION_FACTORY_LOOKUP,
                "ConnectionFactory");
        properties.put(JMSUtils.JMS_PROVIDER_URL, "tcp://localhost:61619");
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61619");
        broker.start();

        return broker;
    }
}
