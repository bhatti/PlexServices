package com.plexobject.bugger.service;

import org.apache.log4j.Logger;


import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.service.LifecycleAware;

public class AbstractService implements LifecycleAware {
    protected final Logger log = Logger.getLogger(getClass());

    protected final UserRepository userRepository;

    public AbstractService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onCreated() {
        // log.debug("created");
    }

    @Override
    public void onDestroyed() {
        // log.debug("destroyed");
    }

    @Override
    public void onStarted() {
        // log.debug("started");
    }

    @Override
    public void onStopped() {
        // log.debug("stopped");
    }
}
