package com.plexobject.bugger.service.project;

import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.AbstractService;

public abstract class AbstractProjectService extends AbstractService  {
    protected final ProjectRepository projectRepository;

    public AbstractProjectService(ProjectRepository projectRepository,
            final UserRepository userRepository) {
        super(userRepository);
        this.projectRepository = projectRepository;
    }
}
