package com.plexobject.bugger.service.project.membership;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.project.AbstractProjectService;
import com.plexobject.domain.ValidationException;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, rolesAllowed = "Manager", endpoint = "/projects/{id}/membership/add", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Void.class, rolesAllowed = "Manager", endpoint = "queue:{scope}-add-project-member-service-queue", method = Method.LISTEN, contentType = "application/json")
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
        boolean projectLead = "true".equals(request.getProperty("projectLead"));
        ValidationException
                .builder()
                .addErrorIfNull(projectId, "undefined_projectId", "projectId",
                        "projectId not specified")
                .addErrorIfNull(assignedTo, "undefined_assignedTo",
                        "assignedTo", "assignedTo not specified")
                .raiseIfHasErrors();

        Project project = projectRepository.load(Long.valueOf(projectId));
        ValidationException
                .builder()
                .addErrorIfNull(project, "projectNotFound", "project",
                        "project not found").raiseIfHasErrors();
        if (projectLead) {
            project.setProjectLead(assignedTo);
        } else {
            project.addMember(assignedTo);
        }
        request.getResponseBuilder().sendSuccess(project);
    }
}
