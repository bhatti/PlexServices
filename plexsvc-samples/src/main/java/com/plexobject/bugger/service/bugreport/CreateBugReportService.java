package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.security.RolesAllowed;
import com.plexobject.service.Service;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceException;
import com.plexobject.service.ServiceRequest;

public class CreateBugReportService extends AbstractBugReportService implements
        Service {
    public CreateBugReportService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can create bug report
    @RolesAllowed("Employee")
    @ServiceConfig(path = "/bugreports", method = "POST", contentType = "application/json")
    @Override
    public void execute(ServiceRequest request) {
        BugReport report = request.getRequest();
        ServiceException
                .builder()
                .addErrorIfNull(report, "undefined_report", "report",
                        "report not specified").raiseIfHasErrors();
        BugReport saved = bugReportRepository.save(report);
        request.getServiceResponseBuilder().setReply(saved).send();
    }

}
