package com.plexobject.bugger.service.project.membership;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.project.AbstractProjectService;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = Project.class, rolesAllowed = "Manager", endpoint = "/projects/{id}/membership", method = Method.POST, contentType = "application/json")
public class AddProjectMemberService extends AbstractProjectService implements
        RequestHandler {
    public AddProjectMemberService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    @Override
    public void handle(Request request) {
        String projectId = request.getProperty("id");
        String assignedTo = request.getProperty("assignedTo");
        String role = request.getProperty("role");
        ServiceException
                .builder()
                .addErrorIfNull(projectId, "undefined_projectId", "projectId",
                        "projectId not specified")
                .addErrorIfNull(assignedTo, "undefined_assignedTo",
                        "assignedTo", "assignedTo not specified")
                .addErrorIfNull(role, "undefined_role", "role",
                        "role not specified").raiseIfHasErrors();

        User assignedToUser = userRepository.load(Long.valueOf(assignedTo));
        Project project = projectRepository.load(Long.valueOf(projectId));
        ServiceException
                .builder()
                .addErrorIfNull(assignedToUser, "assignedToNotFound",
                        "assignedTo", "assignedTo not found")
                .addErrorIfNull(project, "projectNotFound", "project",
                        "project not found").raiseIfHasErrors();
        if ("lead".equals(role)) {
            project.setProjectLead(assignedToUser);
        } else {
            project.addMember(assignedToUser);
        }
        request.getResponseBuilder().send();
    }
}