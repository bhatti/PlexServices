package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{id}", method = Method.POST, contentType = "application/json")
public class UpdateBugReportService extends AbstractBugReportService implements
        RequestHandler {
    public UpdateBugReportService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can update bug report
    @Override
    public void handle(Request request) {
        String id = request.getProperty("id");
        BugReport report = request.getObject();
        ServiceException
                .builder()
                .addErrorIfNull(id, "undefined_id", "id", "id not specified")
                .addErrorIfNull(report, "undefined_report", "report",
                        "report not specified").raiseIfHasErrors();
        report.setId(Long.valueOf(id));
        BugReport saved = bugReportRepository.save(report);
        request.getResponseBuilder().setReply(saved).send();
    }
}
