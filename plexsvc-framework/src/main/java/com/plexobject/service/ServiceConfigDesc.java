package com.plexobject.service;

public class ServiceConfigDesc {
    private final ServiceConfig config;
    private final ServiceScope scope;
    private int state;

    public ServiceConfigDesc(final ServiceConfig config,
            final ServiceScope scope) {
        this.config = config;
        this.scope = scope;
    }

    public ServiceConfig getConfig() {
        return config;
    }

    public ServiceScope getScope() {
        return scope;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

}
