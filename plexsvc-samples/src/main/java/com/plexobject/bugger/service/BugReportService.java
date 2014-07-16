package com.plexobject.bugger.service;

import java.util.Collection;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.predicate.Predicate;
import com.plexobject.security.RolesAllowed;
import com.plexobject.service.ServiceException;

public class BugReportService {
    private final BugReportRepository bugReportRepository;
    private final UserRepository userRepository;

    public BugReportService(BugReportRepository bugReportRepository,
            final UserRepository userRepository) {
        this.bugReportRepository = bugReportRepository;
        this.userRepository = userRepository;
    }

    @RolesAllowed("Employee")
    // any employee who is member of same project can create bug report
    public BugReport create(BugReport report) {
        ServiceException
                .builder()
                .addErrorIfNull(report, "undefined_report", "report",
                        "report not specified").raiseIfHasErrors();
        return bugReportRepository.save(report);
    }

    @RolesAllowed("Employee")
    // any employee who is member of same project can edit bug report
    public BugReport edit(BugReport report) {
        ServiceException
                .builder()
                .addErrorIfNull(report, "undefined_report", "report",
                        "report not specified").raiseIfHasErrors();
        return bugReportRepository.save(report);
    }

    @RolesAllowed("Employee")
    // any employee who is member of same project can assign bug report
    public void assign(Long bugReportId, String assignedTo) {
        ServiceException
                .builder()
                .addErrorIfNull(bugReportId, "undefined_bugReportId",
                        "bugReportId", "bugReportId not specified")
                .addErrorIfNull(assignedTo, "undefined_assignedTo",
                        "assignedTo", "assignedTo not specified")
                .raiseIfHasErrors();

        User assignedToUser = userRepository.load(assignedTo);
        BugReport report = bugReportRepository.load(bugReportId);
        ServiceException
                .builder()
                .addErrorIfNull(assignedToUser, "assignedToNotFound",
                        "assignedTo", "assignedTo not found")
                .addErrorIfNull(report, "bugReportIdNotFound", "bugReportId",
                        "bugReportId not found").raiseIfHasErrors();
        report.setAssignedTo(assignedToUser);
        bugReportRepository.save(report);
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
