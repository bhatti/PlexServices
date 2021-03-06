package com.plexobject.jms;

import com.plexobject.domain.Configuration;

/**
 * This factory is used to create JMS container. PlexServices comes with default
 * implementation but you can implement Spring or Apache Camel if required
 * 
 * @author shahzad bhatti
 *
 */
public interface JMSContainerFactory {
    JMSContainer create(Configuration config);
}
