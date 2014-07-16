package com.plexobject.bugger.service;

import com.plexobject.bugger.model.Project;
import com.plexobject.security.RolesAllowed;

public class ProjectService {
    @RolesAllowed("Manager")
    // Each employee and project lead assigned to a single project
    public Project create(Project project) {
        return project;
    }

    @RolesAllowed("Manager")
    public Project edit(Project project) {
        return project;
    }

    @RolesAllowed("Manager")
    public void delete(Long projectId) {
    }
}
