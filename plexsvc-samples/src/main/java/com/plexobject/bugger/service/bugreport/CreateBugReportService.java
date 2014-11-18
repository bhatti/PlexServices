package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;

//@ServiceConfig(protocol = Protocol.HTTP, requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(protocol = Protocol.JMS, requestClass = BugReport.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-create-bugreport-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
public class CreateBugReportService extends AbstractBugReportService implements
        RequestHandler {
    public CreateBugReportService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can create bug report
    @Override
    public void handle(Request request) {
        BugReport report = request.getPayload();
        report.validate();
        BugReport saved = bugReportRepository.save(report);
        request.getResponseDispatcher().send(saved);
    }

}
