package com.plexobject.bugger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.service.LifecycleAware;
import com.plexobject.service.Pingable;

public class AbstractService implements LifecycleAware, Pingable {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final UserRepository userRepository;

    public AbstractService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public int ping() {
        return 0;
    }

    @Override
    public void onCreated() {
        log.info("created");
    }

    @Override
    public void onDestroyed() {
        log.info("destroyed");
    }

    @Override
    public void onStarted() {
        log.info("started");
    }

    @Override
    public void onStopped() {
        log.info("stopped");
    }
}
