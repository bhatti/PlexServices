package com.plexobject.bugger.service.bugreport.comment;

import java.util.ArrayList;
import java.util.Collection;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.Comment;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.bugreport.AbstractBugReportService;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Comment.class, rolesAllowed = "Employee", endpoint = "/comments", method = Method.GET, codec = CodecType.BINARY)
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Comment.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-query-comments-service-queue", method = Method.GET, codec = CodecType.BINARY)
public class QueryCommentService extends AbstractBugReportService implements
        RequestHandler {
	public QueryCommentService(BugReportRepository bugReportRepository,
	        UserRepository userRepository) {
		super(bugReportRepository, userRepository);
	}

	@Override
	public void handle(Request request) {
		final Collection<Comment> comments = new ArrayList<>();
		Collection<BugReport> reports = bugReportRepository
		        .getAll(new Predicate<BugReport>() {

			        @Override
			        public boolean accept(BugReport report) {
				        return true;
			        }
		        });
		for (BugReport r : reports) {
			comments.addAll(r.getComments());
		}
		request.getResponseBuilder().send(comments);
	}
}
