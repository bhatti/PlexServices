package com.plexobject.javaws;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.Path;

import com.plexobject.bugger.model.BugReport;
import com.plexobject.predicate.Predicate;

@WebService
@Path("/bugReports")
public class BugReportServiceImpl implements BugReportService {
    @Override
    public BugReport create(@WebParam(name = "bugReport") BugReport report) {
        report.validate();
        BugReport saved = SharedRepository.bugReportRepository.save(report);
        return saved;
    }

    @Override
    public BugReport get(final Long id) {
        List<BugReport> reports = SharedRepository.bugReportRepository
                .getAll(new Predicate<BugReport>() {
                    @Override
                    public boolean accept(final BugReport report) {
                        return report.getId().equals(id);
                    }
                });
        return reports.size() > 0 ? reports.get(0) : null;
    }

    @Override
    public List<BugReport> getAll(final Collection<Long> ids) {
        return SharedRepository.bugReportRepository
                .getAll(new Predicate<BugReport>() {
                    @Override
                    public boolean accept(final BugReport report) {
                        return ids.contains(report.getId());
                    }
                });
    }

    @Override
    public List<BugReport> query(Map<String, Object> params) {
        final Long projectId = params.containsKey("projectId") ? (Long) params
                .get("projectId") : null;
        final long since = params.containsKey("since") ? ((Long) params
                .get("since")) : 0;
        final boolean overdue = params.containsKey("overdue") ? (Boolean) params
                .get("overdue") : false;
        final boolean unassigned = params.containsKey("unassigned") ? (Boolean) params
                .get("unassigned") : false;
        final long now = System.currentTimeMillis();

        return SharedRepository.bugReportRepository
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
    }

    @WebMethod(exclude = true)
    public BugReport assignBugReport(Long bugReportId, String assignedTo) {
        BugReport report = SharedRepository.bugReportRepository.load(Long
                .valueOf(bugReportId));
        report.setAssignedTo(assignedTo);
        SharedRepository.bugReportRepository.save(report);
        return report;
    }

}
