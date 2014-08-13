package com.plexobject.service.jms;

import javax.jms.Destination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.domain.Constants;
import com.plexobject.handler.AbstractResponseBuilder;
import com.plexobject.service.ServiceConfig;

/**
 * This class sends reply back over JMS
 * 
 * @author shahzad bhatti
 *
 */
public class JmsResponseBuilder extends AbstractResponseBuilder {
	private static final Logger log = LoggerFactory
	        .getLogger(JmsResponseBuilder.class);
	private final JmsClient jmsClient;
	private final Destination replyTo;

	public JmsResponseBuilder(final ServiceConfig config, JmsClient jmsClient,
	        Destination replyTo) {
		super(config.codec());
		this.jmsClient = jmsClient;
		this.replyTo = replyTo;
	}

	protected void doSend(String payload) {
		try {
			jmsClient.send(replyTo, properties, payload);
			if (log.isDebugEnabled()) {
				log.debug("Sending reply " + payload + " to " + replyTo);
			}
		} catch (Exception e) {
			log.error("Failed to send " + payload, e);
		}
	}

	@Override
	public void addSessionId(String value) {
		properties.put(Constants.SESSION_ID, value);
	}
}
