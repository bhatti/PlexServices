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
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;
import com.plexobject.validation.ValidationException;

//@ServiceConfig(protocol = Protocol.HTTP, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{id}/assign", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Employee", endpoint = "queue://{scope}-assign-bugreport-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "projectId"),
        @Field(name = "bugReportId"),
        @Field(name = "assignedTo") })
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

        BugReport report = bugReportRepository.load(Long.valueOf(bugReportId));
        ValidationException
                .builder()
                .assertNonNull(report, "bugReportIdNotFound", "bugReport",
                        "bugReport not found").end();
        report.setAssignedTo(assignedTo);
        bugReportRepository.save(report);
        request.getResponseDispatcher().send(report);
    }

}
