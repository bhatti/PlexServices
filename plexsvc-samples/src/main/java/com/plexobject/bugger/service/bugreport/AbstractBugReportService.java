package com.plexobject.bugger.service.bugreport;

import java.util.Collection;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.predicate.Predicate;
import com.plexobject.security.RolesAllowed;
import com.plexobject.service.ServiceException;

public class AbstractBugReportService {
    protected final BugReportRepository bugReportRepository;
    protected final UserRepository userRepository;

    public AbstractBugReportService(BugReportRepository bugReportRepository,
            final UserRepository userRepository) {
        this.bugReportRepository = bugReportRepository;
        this.userRepository = userRepository;
    }

    @RolesAllowed("Employee")
    public Collection<BugReport> getBugReportsForProject(final Long projectId,
            final long since) {
        ServiceException
                .builder()
                .addErrorIfNull(projectId, "projectIdUndefined", "projectId",
                        "projectId not defined").raiseIfHasErrors();
        return bugReportRepository.getAll(new Predicate<BugReport>() {

            @Override
            public boolean accept(BugReport report) {
                if (!report.getId().equals(projectId)) {
                    return false;
                }
                if (since != 0 && report.getCreatedAt().getTime() < since) {
                    return false;
                }
                return true;
            }
        });
    }

    @RolesAllowed("Manager")
    public Collection<BugReport> getOverdueBugReports() {
        final long now = System.currentTimeMillis();
        return bugReportRepository.getAll(new Predicate<BugReport>() {
            @Override
            public boolean accept(BugReport report) {
                if (report.getEstimatedResolutionDate() != null
                        && report.getEstimatedResolutionDate().getTime() < now) {
                    return false;
                }
                return true;
            }
        });
    }

    @RolesAllowed("Manager")
    public Collection<BugReport> getUnassignedBugReports() {
        return bugReportRepository.getAll(new Predicate<BugReport>() {
            @Override
            public boolean accept(BugReport report) {
                return report.getAssignedTo() == null;
            }
        });
    }
}
