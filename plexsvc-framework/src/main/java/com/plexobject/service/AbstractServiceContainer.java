package com.plexobject.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.handler.RequestHandler;
import com.plexobject.util.Configuration;

/**
 * This is abstract class that provides helper methods for ServiceContainer
 * interface
 * 
 * @author shahzad bhatti
 *
 */
public abstract class AbstractServiceContainer implements ServiceContainer {
	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected final ServiceRegistry serviceRegistry;
	protected final Configuration config;
	protected boolean running;

	public AbstractServiceContainer(Configuration config,
			ServiceRegistry serviceRegistry) {
		this.config = config;
		this.serviceRegistry = serviceRegistry;
	}

	protected abstract void doStart() throws Exception;

	protected abstract void doStop() throws Exception;

	@Override
	public final synchronized void start() {
		try {
			if (!running) {
				doStart();
				for (RequestHandler h : getHandlers()) {
					if (h instanceof LifecycleAware) {
						((LifecycleAware) h).onStarted();
					}
				}
			}
			log.info("Started");
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
			log.info("Stopped");
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
