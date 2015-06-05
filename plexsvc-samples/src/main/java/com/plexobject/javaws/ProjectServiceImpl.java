package com.plexobject.javaws;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.plexobject.bugger.model.Project;
import com.plexobject.predicate.Predicate;

@WebService
public class ProjectServiceImpl implements ProjectService {
    public Project create(Project project) {
        Project saved = SharedRepository.projectRepository.save(project);
        return saved;
    }       
    @WebMethod(exclude=true)
    public Project addMember(String projectId, String assignedTo, boolean projectLead) {
        Project project = SharedRepository.projectRepository.load(Long.valueOf(projectId));
        if (projectLead) {
            project.setProjectLead(assignedTo);
        } else {
            project.addMember(assignedTo);
        }
        return project;
    }

    @WebMethod(exclude=true)
    public Project removeMember(String projectId, String assignedTo, boolean projectLead) {
        Project project = SharedRepository.projectRepository.load(Long.valueOf(projectId));
        if (projectLead) {
            project.setProjectLead(null);
        } else {
            project.removeMember(assignedTo);
        }
        return project;
    }

    public List<Project> query() {
        return SharedRepository.projectRepository
                .getAll(new Predicate<Project>() {

                    @Override
                    public boolean accept(Project project) {
                        return true;
                    }
                });
    }
}
