package com.plexobject.bugger.service.notification;

import org.apache.log4j.Logger;


import com.plexobject.bugger.model.BugReport;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;

//@ServiceConfig(protocol = Protocol.HTTP, rolesAllowed = "Employee", endpoint = "/notify", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Employee", endpoint = "queue://{scope}-notify-service", method = Method.MESSAGE, codec = CodecType.JSON)
public class NotificationService implements RequestHandler {
    private static final Logger log = Logger
            .getLogger(NotificationService.class);

    public void sendNotification(BugReport report) {
        log.info("Notifying " + report);
    }

    // any employee who is member of same project can update bug report
    @Override
    public void handle(Request request) {
        log.info("Notifying " + request);
    }
}
