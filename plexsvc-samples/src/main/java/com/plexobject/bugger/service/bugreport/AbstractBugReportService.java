package com.plexobject.bugger.service.bugreport;

import com.plexobject.bugger.repository.BugReportRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.AbstractService;

public class AbstractBugReportService extends AbstractService  {
    protected final BugReportRepository bugReportRepository;

    public AbstractBugReportService(BugReportRepository bugReportRepository,
            final UserRepository userRepository) {
        super(userRepository);
        this.bugReportRepository = bugReportRepository;
    }
}
