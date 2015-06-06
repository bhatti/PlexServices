package com.plexobject.javaws;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;

import com.plexobject.bugger.model.BugReport;

@WebService
public interface BugReportService {
    BugReport get(Long id);
    
    BugReport create(BugReport report);

    List<BugReport> getAll(Collection<Long> ids);

    List<BugReport> query(Map<String, Object> params);

    BugReport assignBugReport(Long bugReportId, String assignedTo);
}
