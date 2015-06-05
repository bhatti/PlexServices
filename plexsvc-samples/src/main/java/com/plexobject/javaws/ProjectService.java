package com.plexobject.javaws;

import java.util.List;

import javax.jws.WebService;

import com.plexobject.bugger.model.Project;

@WebService
public interface ProjectService {
    Project create(Project project); 
    Project addMember(String projectId, String assignedTo, boolean projectLead); 
    Project removeMember(String projectId, String assignedTo, boolean projectLead); 
    List<Project> query(); 
}
