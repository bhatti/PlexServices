package com.plexobject.bugger.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.security.RolesAllowed;
import com.plexobject.service.Service;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceRequest;

public class NotificationService implements Service {
    private static final Logger logger = LoggerFactory
            .getLogger(NotificationService.class);

    public void sendNotification(BugReport report) {
        logger.info("Notifying " + report);
    }

    // any employee who is member of same project can update bug report
    @RolesAllowed("Employee")
    @ServiceConfig(path = "/notify", method = "POST", contentType = "application/json")
    @Override
    public void execute(ServiceRequest request) {
        logger.info("Notifying " + request);
    }
}
