package com.plexobject.bugger.service.bugreport.comment;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.Comment;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.bugreport.AbstractBugReportService;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = Comment.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{bugId}/comments", method = Method.POST, contentType = "application/json")
public class CreateCommentService extends AbstractBugReportService implements
        RequestHandler {
    public CreateCommentService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can create comment
    @Override
    public void handle(Request request) {
        String projectId = request.getProperty("projectId");
        String bugId = request.getProperty("bugId");
        ServiceException
                .builder()
                .addErrorIfNull(projectId, "undefined_projectId", "projectId",
                        "projectId not specified")
                .addErrorIfNull(bugId, "undefined_bugId", "bugId",
                        "bugId not specified").raiseIfHasErrors();

        Comment comment = request.getObject();
        BugReport report = bugReportRepository.load(Long.valueOf(bugId));
        ServiceException
                .builder()
                .addErrorIfNull(report, "undefined_project", "project",
                        "project not specified").raiseIfHasErrors();
        report.getComments().add(comment);
        bugReportRepository.save(report);
        request.getResponseBuilder().setReply(comment).send();
    }
}