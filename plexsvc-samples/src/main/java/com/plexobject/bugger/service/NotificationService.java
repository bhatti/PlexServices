package com.plexobject.bugger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bugger.model.BugReport;

public class NotificationService {
    private static final Logger logger = LoggerFactory
            .getLogger(NotificationService.class);

    public void sendNotification(BugReport report) {
        logger.info("Notifying " + report);
    }
}
