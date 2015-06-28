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
    private static final Logger logger = Logger
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

    public void reset() {
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
            consumer = waitForStartAndCreateConsumer();
            while (!stop) {
                try {
                    Message msg = timeout > 0 ? consumer.receive(timeout)
                            : consumer.receive();
                    if (msg != null) {
                        messageListener.onMessage(msg);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("PLEXSVC Waiting for JMS message on "
                                    + destination);
                        }
                    }
                } catch (JMSException e) {
                    if (e.getCause() instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        logger.error("PLEXSVC interrupted thread while waiting for "
                                + destination + ", giving up...");
                    } else {
                        logger.error("PLEXSVC Failed to receive message for "
                                + destination + ", notifying errors..", e);
                        if (exceptionListener != null) {
                            exceptionListener.onException(e);
                        }
                    }
                    break;
                }
            }
        } catch (JMSException e) {
            if (e.getCause() instanceof java.io.EOFException) {
                logger.warn("PLEXSVC Failed to create consumer in attempt to retry"
                        + e);
            } else if (e.getMessage().contains("channel has already failed")) {
                logger.warn("PLEXSVC Failed to create consumer in attempt to retry"
                        + e);
            } else {
                logger.error("PLEXSVC Failed to create consumer", e);
                if (exceptionListener != null) {
                    exceptionListener.onException(e);
                }
            }
        } catch (NamingException e) {
            logger.error("PLEXSVC Failed to lookup destination", e);
            if (exceptionListener != null) {
                exceptionListener.onException(new JMSException(e.toString()));
            }
        } finally {
            logger.info("PLEXSVC Exiting for destination " + destination
                    + ", closing consumer");
            afterRun(consumer);
        }
    }

    private MessageConsumer waitForStartAndCreateConsumer()
            throws JMSException, NamingException {
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
                consumer.close();
            }
        } catch (Exception e) {
            logger.error("PLEXSVC Failed to close consumer for " + destination);
        }
        callback.onStopped(this);
    }

}
