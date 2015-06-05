package com.plexobject.javaws;

import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.ProjectRepository;

public class SharedRepository {
    static final BugReportRepository bugReportRepository = new BugReportRepository();
    static final ProjectRepository projectRepository = new ProjectRepository();

}
