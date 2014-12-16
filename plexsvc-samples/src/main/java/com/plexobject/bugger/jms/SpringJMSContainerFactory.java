package com.plexobject.bugger.jms;

import javax.naming.NamingException;

import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSContainerFactory;
import com.plexobject.util.Configuration;

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