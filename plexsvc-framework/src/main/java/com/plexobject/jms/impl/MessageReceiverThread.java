package com.plexobject.jms.impl;

import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.plexobject.domain.Preconditions;

/**
 * This class listens for incoming JMS messages in a separate thread
 * 
 * @author shahzad bhatti
 *
 */
public class MessageReceiverThread implements Runnable {
    private static final int DEFAULT_TIMEOUT = 15000;
    private static final Logger log = Logger
            .getLogger(MessageReceiverThread.class);

    interface Callback {
        void onStarted(MessageReceiverThread t);

        void onStopped(MessageReceiverThread t);
    }

    private final String threadName;
    private final Destination destination;
    private final MessageListener messageListener;
    private ExceptionListener exceptionListener;
    private final Callback callback;
    private final DefaultJMSContainer jmsContainer;
    private final long timeout;
    private volatile boolean stop;
    private volatile boolean running;
    private volatile Thread runnerThread;

    public MessageReceiverThread(String threadName, Destination destination,
            MessageListener messageListener,
            ExceptionListener exceptionListener, Callback callback,
            long timeout, DefaultJMSContainer jmsContainer) {
        Preconditions.checkEmpty(threadName, "threadName is not specified");
        Preconditions.requireNotNull(destination,
                "destination is not specified");
        Preconditions.requireNotNull(messageListener,
                "messageListener is not specified");
        Preconditions.requireNotNull(jmsContainer,
                "jmsContainer is not specified");
        Preconditions.requireNotNull(callback, "callback is not specified");
        this.threadName = threadName;
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
        if (runnerThread != null) {
            runnerThread.interrupt();
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
                            : consumer.receive(DEFAULT_TIMEOUT);
                    if (msg != null) {
                        messageListener.onMessage(msg);
                    } else {
                        log.info("**** Waiting for JMS message on "
                                + destination);
                    }
                } catch (JMSException e) {
                    log.error("Failed to receive message for " + destination, e);
                    if (exceptionListener != null) {
                        exceptionListener.onException(e);
                    }
                    break;
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
        runnerThread = Thread.currentThread();
        Thread.currentThread().setName(threadName);
        jmsContainer.waitUntilReady();
        MessageConsumer consumer = jmsContainer.createConsumer(destination);
        callback.onStarted(this);
        running = true;
        return consumer;
    }

    private void afterRun(MessageConsumer consumer) {
        running = false;
        runnerThread = null;
        try {
            if (consumer != null) {
                log.warn("*** CLOSING CONSUMER " + destination);
                consumer.close();
            }
        } catch (Exception e) {
            log.error("Failed to close consumer for " + destination);
        }
        callback.onStopped(this);
    }

}
