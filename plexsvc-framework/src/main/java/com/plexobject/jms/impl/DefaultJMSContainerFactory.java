package com.plexobject.jms.impl;

import com.plexobject.domain.Configuration;
import com.plexobject.jms.JMSContainer;
import com.plexobject.jms.JMSContainerFactory;

public class DefaultJMSContainerFactory implements JMSContainerFactory {
    @Override
    public JMSContainer create(Configuration config) {
        return new DefaultJMSContainer(config);
    }

}
