package com.plexobject.service;

import java.util.Collection;

public interface ServiceRegistryMBean extends Lifecycle {
    Collection<ServiceConfigDesc> getServiceConfigurations();

    String dumpServiceConfigurations();
}