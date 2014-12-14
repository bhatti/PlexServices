package com.plexobject.jms;

import com.plexobject.util.Configuration;

/**
 * This factory is used to create JMS container. PlexService comes with default
 * implementation but you can implement Spring or Apache Camel if required
 * 
 * @author shahzad bhatti
 *
 */
public interface JMSContainerFactory {
    JMSContainer create(Configuration config);
}
