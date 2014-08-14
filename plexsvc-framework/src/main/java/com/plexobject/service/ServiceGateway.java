package com.plexobject.service;

import java.util.Collection;

import com.plexobject.handler.RequestHandler;

/**
 * This interface defines gateway for adding/removing handlers
 * 
 * @author shahzad bhatti
 *
 */
public interface ServiceGateway extends Lifecycle {
	void add(RequestHandler handler);

	boolean remove(RequestHandler handler);

	boolean exists(RequestHandler handler);

	Collection<RequestHandler> getHandlers();
}
