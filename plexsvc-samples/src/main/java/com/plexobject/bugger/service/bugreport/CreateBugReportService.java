package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports", method = Method.POST, contentType = "application/json")
public class CreateBugReportService extends AbstractBugReportService implements
        RequestHandler {
    public CreateBugReportService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can create bug report
    @Override
    public void handle(Request request) {
        BugReport report = request.getObject();
        ServiceException
                .builder()
                .addErrorIfNull(report, "undefined_report", "report",
                        "report not specified").raiseIfHasErrors();
        BugReport saved = bugReportRepository.save(report);
        request.getResponseBuilder().setReply(saved).send();
    }

}
