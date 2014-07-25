package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.security.RolesAllowed;
import com.plexobject.service.Service;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceException;
import com.plexobject.service.ServiceRequest;

public class AssignBugReportService extends AbstractBugReportService implements
        Service {
    public AssignBugReportService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can assign bug report
    @RolesAllowed("Employee")
    @ServiceConfig(path = "/bugreports/{id}/assign", method = "POST", contentType = "application/json")
    @Override
    public void execute(ServiceRequest request) {
        String bugReportId = request.getProperty("id");
        String assignedTo = request.getProperty("assignedTo");
        ServiceException
                .builder()
                .addErrorIfNull(bugReportId, "undefined_bugReportId",
                        "bugReportId", "bugReportId not specified")
                .addErrorIfNull(assignedTo, "undefined_assignedTo",
                        "assignedTo", "assignedTo not specified")
                .raiseIfHasErrors();

        User assignedToUser = userRepository.load(assignedTo);
        BugReport report = bugReportRepository.load(Long.valueOf(bugReportId));
        ServiceException
                .builder()
                .addErrorIfNull(assignedToUser, "assignedToNotFound",
                        "assignedTo", "assignedTo not found")
                .addErrorIfNull(report, "bugReportIdNotFound", "bugReportId",
                        "bugReportId not found").raiseIfHasErrors();
        report.setAssignedTo(assignedToUser);
        bugReportRepository.save(report);
        request.getServiceResponseBuilder().send();
    }

}
