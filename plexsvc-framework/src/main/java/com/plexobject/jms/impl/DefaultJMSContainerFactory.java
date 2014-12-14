package com.plexobject.jms.impl;

import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSContainerFactory;
import com.plexobject.util.Configuration;

public class DefaultJMSContainerFactory implements JMSContainerFactory {
    @Override
    public JMSContainer create(Configuration config) {
        return new DefaultJMSContainer(config);
    }

}
