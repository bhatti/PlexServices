package com.plexobject.jaxws;

import java.util.Collection;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;

import com.plexobject.bugger.model.Project;
import com.plexobject.predicate.Predicate;

@WebService
@Path("/projects")
public class ProjectServiceImpl implements ProjectService {
    @Override
    public Project create(Project project) {
        Project saved = SharedRepository.projectRepository.save(project);
        return saved;
    }

    @Override
    @WebMethod(exclude = true)
    public Project addMember(String projectId, String assignedTo,
            boolean projectLead) {
        Project project = SharedRepository.projectRepository.load(Long
                .valueOf(projectId));
        if (projectLead) {
            project.setProjectLead(assignedTo);
        } else {
            project.addMember(assignedTo);
        }
        return project;
    }

    @Override
    @WebMethod(exclude = true)
    public Project removeMember(String projectId, String assignedTo,
            boolean projectLead) {
        Project project = SharedRepository.projectRepository.load(Long
                .valueOf(projectId));
        if (projectLead) {
            project.setProjectLead(null);
        } else {
            project.removeMember(assignedTo);
        }
        return project;
    }

    @Override
    public List<Project> query() {
        return SharedRepository.projectRepository
                .getAll(new Predicate<Project>() {

                    @Override
                    public boolean accept(Project project) {
                        return true;
                    }
                });
    }

    @Override
    public int verifyProjects(Collection<Project> projects) {
        return 0;
    }
}
