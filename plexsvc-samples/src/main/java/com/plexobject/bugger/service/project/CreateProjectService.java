package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Project.class, rolesAllowed = "Manager", endpoint = "/projects", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Project.class, rolesAllowed = "Manager", endpoint = "queue:{scope}-create-projects-service-queue", method = Method.LISTEN, contentType = "application/json")
public class CreateProjectService extends AbstractProjectService implements
        RequestHandler {
    public CreateProjectService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    // Each employee and project lead assigned to a single project
    @Override
    public void handle(Request request) {
        Project project = request.getPayload();
        ValidationException
                .builder()
                .addErrorIfNull(project, "undefined_project", "project",
                        "project not specified").raiseIfHasErrors();
        Project saved = projectRepository.save(project);
        request.getResponseBuilder().sendSuccess(saved);
    }

}
