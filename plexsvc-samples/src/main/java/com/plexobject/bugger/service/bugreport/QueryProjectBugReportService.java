package com.plexobject.bugger.service.bugreport;

import java.util.Collection;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "/projects/{projectId}/bugreports", method = Method.GET, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Void.class, rolesAllowed = "Employee", endpoint = "queue:{scope}-query-project-bugreport-service-queue", method = Method.LISTEN, contentType = "application/json")
public class QueryProjectBugReportService extends AbstractBugReportService
        implements RequestHandler {
    public QueryProjectBugReportService(
            BugReportRepository bugReportRepository,
            UserRepository userRepository) {
        super(bugReportRepository, userRepository);
    }

    @Override
    public void handle(Request request) {
        final Long projectId = Long.valueOf(request.getProperty("projectId"));
        final long since = request.getProperty("since") == null ? 0 : Long
                .valueOf(request.getProperty("since"));
        final boolean overdue = "true".equals(request.getProperty("overdue"));
        final boolean unassigned = "true".equals(request
                .getProperty("unassigned"));
        final long now = System.currentTimeMillis();

        Collection<BugReport> reports = bugReportRepository
                .getAll(new Predicate<BugReport>() {

                    @Override
                    public boolean accept(BugReport report) {
                        if (!report.getProjectId().equals(projectId)) {
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
        request.getResponseBuilder().sendSuccess(reports);
    }

}
