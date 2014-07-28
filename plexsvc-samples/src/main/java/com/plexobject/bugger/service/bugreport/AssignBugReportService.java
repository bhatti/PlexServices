package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = Void.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{id}/assign", method = Method.POST, contentType = "application/json")
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
        String assignedTo = request.getProperty("assignedTo");
        ServiceException
                .builder()
                .addErrorIfNull(bugReportId, "undefined_bugReportId",
                        "bugReportId", "bugReportId not specified")
                .addErrorIfNull(assignedTo, "undefined_assignedTo",
                        "assignedTo", "assignedTo not specified")
                .raiseIfHasErrors();

        User assignedToUser = userRepository.load(Long.valueOf(assignedTo));
        BugReport report = bugReportRepository.load(Long.valueOf(bugReportId));
        ServiceException
                .builder()
                .addErrorIfNull(assignedToUser, "assignedToNotFound",
                        "assignedTo", "assignedTo not found")
                .addErrorIfNull(report, "bugReportIdNotFound", "bugReport",
                        "bugReport not found").raiseIfHasErrors();
        report.setAssignedTo(assignedToUser);
        bugReportRepository.save(report);
        request.getResponseBuilder().send();
    }

}
