package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(protocol = Protocol.HTTP, requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{id}", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(protocol = Protocol.JMS, requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-update-bugreport-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
public class UpdateBugReportService extends AbstractBugReportService implements
        RequestHandler {
	public UpdateBugReportService(BugReportRepository bugReportRepository,
	        UserRepository userRepository) {
		super(bugReportRepository, userRepository);
	}

	// any employee who is member of same project can update bug report
	@Override
	public void handle(Request request) {
		BugReport report = request.getPayload();
		ValidationException
		        .builder()
		        .assertNonNull(report.getId(), "undefined_id", "id",
		                "id not specified")
		        .assertNonNull(report.getProjectId(), "undefined_projectId",
		                "projectId", "projectId not specified").end();
		BugReport saved = bugReportRepository.save(report);
		request.getResponseDispatcher().send(saved);
	}
}
