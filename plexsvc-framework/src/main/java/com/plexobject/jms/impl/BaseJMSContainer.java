package com.plexobject.jms.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.apache.log4j.Logger;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Constants;
import com.plexobject.jms.DestinationResolver;
import com.plexobject.jms.JMSContainer;

public abstract class BaseJMSContainer implements JMSContainer,
        ExceptionListener {
    protected final Logger logger = Logger.getLogger(getClass());
    protected final Configuration config;
    protected final DestinationResolver destinationResolver;
    protected boolean running;
    protected boolean transactedSession;
    private final List<ExceptionListener> exceptionListeners = new ArrayList<>();

    public BaseJMSContainer(Configuration config) {
        this(config, new DestinationResolverImpl(config));
    }

    public BaseJMSContainer(Configuration config,
            DestinationResolver destinationResolver) {
        this.config = config;
        this.destinationResolver = destinationResolver;
        transactedSession = config.getBoolean(Constants.JMS_TRASACTED_SESSION);
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    @Override
    public void onException(JMSException e) {
        logger.error("PLEXSVC JMS Error " + e);
        final List<ExceptionListener> copyExceptionListeners = new ArrayList<>();

        synchronized (exceptionListeners) {
            copyExceptionListeners.addAll(exceptionListeners);
        }
        for (ExceptionListener l : copyExceptionListeners) {
            l.onException(e);
        }
    }

    @Override
    public void addExceptionListener(ExceptionListener l) {
        synchronized (exceptionListeners) {
            if (!exceptionListeners.contains(l)) {
                exceptionListeners.add(l);
            }
        }
    }
}
