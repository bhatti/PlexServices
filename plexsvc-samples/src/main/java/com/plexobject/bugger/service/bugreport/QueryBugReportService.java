package com.plexobject.bugger.service.bugreport;

import java.util.List;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(protocol = Protocol.HTTP, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "/bugreports", method = Method.GET)
@ServiceConfig(protocol = Protocol.JMS, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-bugreports-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
public class QueryBugReportService extends AbstractBugReportService implements
        RequestHandler {
	public QueryBugReportService(BugReportRepository bugReportRepository,
	        UserRepository userRepository) {
		super(bugReportRepository, userRepository);
	}

	@Override
	public void handle(Request request) {
		final Long projectId = request.hasProperty("projectId") ? request
		        .getLongProperty("projectId") : null;
		final long since = request.hasProperty("since") ? request
		        .getLongProperty("since") : 0;
		final boolean overdue = request.getBooleanProperty("overdue", false);
		final boolean unassigned = request.getBooleanProperty("unassigned",
		        false);
		final long now = System.currentTimeMillis();

		final List<BugReport> reports = bugReportRepository
		        .getAll(new Predicate<BugReport>() {
			        @Override
			        public boolean accept(final BugReport report) {
				        if (projectId != null
				                && !report.getProjectId().equals(projectId)) {
					        return false;
				        }
				        if (since != 0
				                && report.getCreatedAt().getTime() < since) {
					        return false;
				        }
				        if (overdue
				                && report.getEstimatedResolutionDate() != null
				                && report.getEstimatedResolutionDate()
				                        .getTime() < now) {
					        return false;
				        }
				        if (unassigned && report.getAssignedTo() != null) {
					        return false;
				        }

				        return true;
			        }
		        });
		request.getResponseDispatcher().send(reports);
	}
}
