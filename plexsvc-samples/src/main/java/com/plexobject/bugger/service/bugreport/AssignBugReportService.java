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

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{id}/assign", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-assign-bugreport-service-queue", method = Method.LISTEN, contentType = "application/json")
public class AssignBugReportService extends AbstractBugReportService implements
        RequestHandler {
    public AssignBugReportService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can assign bug report
    @Override
    public void handle(Request request) {
        String bugReportId = request.getProperty("id");
        String projectId = request.getProperty("projectId");
        String assignedTo = request.getProperty("assignedTo");
        ValidationException
                .builder()
                .addErrorIfNull(projectId, "undefined_projectId", "projectId",
                        "projectId not specified")
                .addErrorIfNull(bugReportId, "undefined_bugReportId",
                        "bugReportId", "bugReportId not specified")
                .addErrorIfNull(assignedTo, "undefined_assignedTo",
                        "assignedTo", "assignedTo not specified")
                .raiseIfHasErrors();

        BugReport report = bugReportRepository.load(Long.valueOf(bugReportId));
        ValidationException
                .builder()
                .addErrorIfNull(report, "bugReportIdNotFound", "bugReport",
                        "bugReport not found").raiseIfHasErrors();
        report.setAssignedTo(assignedTo);
        bugReportRepository.save(report);
        request.getResponseBuilder().sendSuccess(report);
    }

}
