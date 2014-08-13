package com.plexobject.bugger.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "/notify", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-notify-service", method = Method.MESSAGE, codec = CodecType.JSON)
public class NotificationService implements RequestHandler {
	private static final Logger log = LoggerFactory
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
