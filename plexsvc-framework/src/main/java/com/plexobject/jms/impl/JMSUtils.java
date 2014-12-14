package com.plexobject.jms.impl;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSContainerFactory;
import com.plexobject.util.Configuration;

public class JMSUtils {
    private static final Logger log = LoggerFactory.getLogger(JMSUtils.class);
    private static final String JMS_DESTINATION = "JMSDestination";
    private static final String JMS_REPLY_TO = "JMSReplyTo";
    private static final String JMS_CORRELATION_ID = "JMSCorrelationID";
    private static final String JMS_TIMESTAMP = "JMSTimestamp";
    private static final String JMS_MESSAGE_ID = "JMSMessageID";
    private static boolean sendJmsHeaders;

    static void setHeaders(final Map<String, Object> headers, Message reqMsg)
            throws JMSException {
        for (Map.Entry<String, Object> e : headers.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            if (value instanceof Integer) {
                reqMsg.setIntProperty(name, (Integer) value);
            } else if (value instanceof Long) {
                reqMsg.setLongProperty(name, (Long) value);
            } else if (value instanceof Short) {
                reqMsg.setShortProperty(name, (Short) value);
            } else if (value instanceof Byte) {
                reqMsg.setByteProperty(name, (Byte) value);
            } else if (value instanceof String) {
                reqMsg.setStringProperty(name, (String) value);
            } else if (value instanceof Boolean) {
                reqMsg.setBooleanProperty(name, (Boolean) value);
            } else if (value instanceof Float) {
                reqMsg.setFloatProperty(name, (Float) value);
            } else if (value instanceof Double) {
                reqMsg.setDoubleProperty(name, (Double) value);
            } else {
                log.info("*** key " + name + ", value " + value);
                reqMsg.setStringProperty(e.getKey(), e.getValue().toString());
            }
        }
    }

    static String getDestName(Destination destination) throws JMSException {
        if (destination instanceof Queue) {
            Queue q = (Queue) destination;
            return q.getQueueName();
        } else if (destination instanceof Topic) {
            Topic t = (Topic) destination;
            return t.getTopicName();
        } else {
            return null;
        }
    }

    public static JMSContainer getJMSContainer(Configuration config) {
        String factoryClassName = config
                .getProperty(Constants.PLEXSERVICE_JMS_CONTAINER_FACTORY_CLASS);
        if (factoryClassName == null) {
            return new DefaultJMSContainerFactory().create(config);
        }
        try {
            Class<?> factoryClass = Class.forName(factoryClassName);
            JMSContainerFactory factory = (JMSContainerFactory) factoryClass
                    .getDeclaredConstructor(Configuration.class).newInstance(
                            config);
            return factory.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JMSContainerFactory",
                    e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Map<String, Object> getProperties(Message message)
            throws JMSException {
        Map<String, Object> params = new HashMap<>();
        if (sendJmsHeaders) {
            params.put(JMS_MESSAGE_ID, message.getJMSMessageID());
            params.put(JMS_TIMESTAMP, message.getJMSTimestamp());
            params.put(JMS_CORRELATION_ID, message.getJMSCorrelationID());
            params.put(JMS_REPLY_TO, message.getJMSReplyTo());
            params.put(JMS_DESTINATION, message.getJMSDestination());
        }
        Enumeration headerNames = message.getPropertyNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            params.put(name, message.getObjectProperty(name));
        }
        return params;
    }

}
