package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;

//@ServiceConfig(protocol = Protocol.HTTP, requestClass = Project.class, rolesAllowed = "Manager", endpoint = "/projects", method = Method.POST, contentType = "application/json")
@ServiceConfig(protocol = Protocol.JMS, requestClass = Project.class, rolesAllowed = "Manager", endpoint = "queue:{scope}-create-projects-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
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
                .assertNonNull(project, "undefined_project", "project",
                        "project not specified").end();
        Project saved = projectRepository.save(project);
        request.getResponseDispatcher().send(saved);
    }

}
