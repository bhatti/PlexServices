package com.plexobject.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ServiceRegistration {
    private final Set<Service> services = new HashSet<Service>();

    public ServiceRegistration() {
    }

    public ServiceRegistration(Set<Service> services) {
        this.services.addAll(services);
    }

    public void addService(Service service) {
        this.services.add(service);
    }

    public void removeService(Service service) {
        this.services.remove(service);
    }

    public Collection<Service> getServices() {
        return services;
    }
}
