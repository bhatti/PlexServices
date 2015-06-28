package com.plexobject.service.impl;

import org.apache.log4j.Logger;

import com.plexobject.handler.RequestHandler;
import com.plexobject.service.LifecycleAware;
import com.plexobject.service.ServiceContainer;
import com.plexobject.service.ServiceRegistry;

/**
 * This is abstract class that provides helper methods for ServiceContainer
 * interface
 * 
 * @author shahzad bhatti
 *
 */
public abstract class AbstractServiceContainer implements ServiceContainer {
    protected final Logger logger = Logger.getLogger(getClass());
    protected final ServiceRegistry serviceRegistry;
    protected boolean running;

    public AbstractServiceContainer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    protected abstract void doStart() throws Exception;

    protected abstract void doStop() throws Exception;

    @Override
    public final synchronized void start() {
        try {
            logger.info("PLEXSVC Starting ..." + running + ", handlers "
                    + getHandlers().size());
            if (!running) {
                doStart();
                for (RequestHandler h : getHandlers()) {
                    if (h instanceof LifecycleAware) {
                        ((LifecycleAware) h).onStarted();
                    }
                }
            }
            running = true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final synchronized void stop() {
        try {
            if (running) {
                doStop();
            }
            for (RequestHandler h : getHandlers()) {
                if (h instanceof LifecycleAware) {
                    ((LifecycleAware) h).onStopped();
                }
            }
            running = false;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final synchronized boolean isRunning() {
        return running;
    }
}
