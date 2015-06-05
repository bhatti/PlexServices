package com.plexobject.javaws;

import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import com.plexobject.bugger.model.BugReport;

@WebService
public interface BugReportService {
    BugReport create(BugReport report);

    List<BugReport> query(Map<String, Object> params);

    BugReport assignBugReport(Long bugReportId, String assignedTo);
}
