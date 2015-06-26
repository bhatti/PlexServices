package com.plexobject.jms;

import javax.naming.NamingException;

import com.plexobject.domain.Configuration;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSContainerFactory;

public class SpringJMSContainerFactory implements JMSContainerFactory {
    @Override
    public JMSContainer create(Configuration config) {
        try {
            return new SpringJMSContainer(config);
        } catch (NamingException e) {
            throw new RuntimeException("Failed to create spring jms container",
                    e);
        }
    }

}
