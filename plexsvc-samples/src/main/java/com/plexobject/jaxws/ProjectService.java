package com.plexobject.jaxws;

import java.util.Collection;
import java.util.List;

import javax.jws.WebService;

import com.plexobject.bugger.model.Project;

@WebService
public interface ProjectService {
    int verifyProjects(Collection<Project> projects);
    Project create(Project project); 
    Project addMember(String projectId, String assignedTo, boolean projectLead); 
    Project removeMember(String projectId, String assignedTo, boolean projectLead); 
    List<Project> query(); 
}
