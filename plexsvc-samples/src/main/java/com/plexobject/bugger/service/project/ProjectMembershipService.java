package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.model.User;
import com.plexobject.security.RolesAllowed;

public class ProjectMembershipService {

    @RolesAllowed("Manager")
    // only manager can assign people to projects
    public void addMemberToProject(User user, Project project) {

    }

    @RolesAllowed("Manager")
    // only manager can assign people to projects
    public void addLeadToProject(User user, Project project) {

    }

    @RolesAllowed("Manager")
    public void removeMemberFromProject(User user, Project project) {

    }

    @RolesAllowed("Manager")
    public void removeLeadFromProject(User user, Project project) {

    }

}
