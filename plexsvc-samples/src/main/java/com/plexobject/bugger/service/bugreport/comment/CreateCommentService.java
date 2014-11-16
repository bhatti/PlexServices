package com.plexobject.bugger.service.bugreport.comment;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.Comment;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.bugreport.AbstractBugReportService;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(protocol = Protocol.HTTP, requestClass = Comment.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports/{id}/comments", method = Method.POST)
@ServiceConfig(protocol = Protocol.JMS, requestClass = Comment.class, rolesAllowed = "Employee", endpoint = "queue:create-project-bugreport-comment-service", method = Method.MESSAGE, codec = CodecType.JSON)
public class CreateCommentService extends AbstractBugReportService implements
        RequestHandler {
    public CreateCommentService(BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    // any employee who is member of same project can create comment
    @Override
    public void handle(Request request) {
        Comment comment = request.getPayload();
        comment.validate();
        BugReport report = bugReportRepository.load(Long.valueOf(comment
                .getBugId()));
        ValidationException
                .builder()
                .assertNonNull(report, "undefined_project", "project",
                        "project not specified").end();
        report.getComments().add(comment);
        bugReportRepository.save(report);
        request.getResponseDispatcher().send(comment);
    }
}