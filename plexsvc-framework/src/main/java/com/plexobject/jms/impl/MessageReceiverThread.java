package com.plexobject.jms.impl;

import java.util.Objects;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens for incoming JMS messages in a separate thread
 * 
 * @author shahzad bhatti
 *
 */
public class MessageReceiverThread implements Runnable {
    private static final Logger log = LoggerFactory
            .getLogger(MessageReceiverThread.class);

    interface Callback {
        void onStarted(MessageReceiverThread t);

        void onStopped(MessageReceiverThread t);
    }

    private final String name;
    private final Destination destination;
    private final MessageListener messageListener;
    private ExceptionListener exceptionListener;
    private final Callback callback;
    private final DefaultJMSContainer jmsContainer;
    private final long timeout;
    private volatile boolean stop;
    private volatile boolean running;
    private volatile Thread thread;

    public MessageReceiverThread(String name, Destination destination,
            MessageListener messageListener,
            ExceptionListener exceptionListener, Callback callback,
            long timeout, DefaultJMSContainer jmsContainer) {
        Objects.requireNonNull(name, "name is not specified");
        Objects.requireNonNull(destination, "destination is not specified");
        Objects.requireNonNull(messageListener,
                "messageListener is not specified");
        Objects.requireNonNull(jmsContainer, "jmsContainer is not specified");
        Objects.requireNonNull(callback, "callback is not specified");
        this.name = name;
        this.destination = destination;
        this.messageListener = messageListener;
        this.exceptionListener = exceptionListener;
        this.callback = callback;
        this.timeout = timeout;
        this.jmsContainer = jmsContainer;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        running = false;
        stop = false;
    }

    public void stop() {
        stop = true;
        if (thread != null) {
            thread.interrupt();
        }
    }

    public void setExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    @Override
    public void run() {
        MessageConsumer consumer = null;
        try {
            consumer = beforeRun();
            while (!stop) {
                try {
                    Message msg = timeout > 0 ? consumer.receive(timeout)
                            : consumer.receive();
                    if (msg != null) {
                        messageListener.onMessage(msg);
                    }
                } catch (JMSException e) {
                    log.error("Failed to receive message", e);
                    if (exceptionListener != null) {
                        exceptionListener.onException(e);
                    }
                }
            }
        } catch (JMSException e) {
            log.error("Failed to create consumer", e);
            if (exceptionListener != null) {
                exceptionListener.onException(e);
            }
        } catch (NamingException e) {
            log.error("Failed to lookup destination", e);
            if (exceptionListener != null) {
                exceptionListener.onException(new JMSException(e.toString()));
            }
        } finally {
            afterRun(consumer);
        }
    }

    private MessageConsumer beforeRun() throws JMSException, NamingException {
        thread = Thread.currentThread();
        Thread.currentThread().setName(name);
        jmsContainer.waitUntilReady();
        MessageConsumer consumer = jmsContainer.createConsumer(destination);
        callback.onStarted(this);
        running = true;
        return consumer;
    }

    private void afterRun(MessageConsumer consumer) {
        running = false;
        thread = null;
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (Exception e) {
            log.error("Failed to close consumer for " + destination);
        }
        callback.onStopped(this);
    }

}
