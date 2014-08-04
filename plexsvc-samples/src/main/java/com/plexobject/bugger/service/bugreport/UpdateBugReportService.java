package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{id}", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-update-bugreport-service-queue", method = Method.LISTEN, contentType = "application/json")
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
                .addErrorIfNull(report.getId(), "undefined_id", "id",
                        "id not specified")
                .addErrorIfNull(report.getProjectId(), "undefined_projectId",
                        "projectId", "projectId not specified")
                .raiseIfHasErrors();
        BugReport saved = bugReportRepository.save(report);
        request.getResponseBuilder().sendSuccess(saved);
    }
}
