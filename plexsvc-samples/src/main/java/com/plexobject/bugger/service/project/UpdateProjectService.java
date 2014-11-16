package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(protocol = Protocol.HTTP,requestClass = Project.class, rolesAllowed = "Manager", endpoint = "/projects/{id}", method = Method.POST, contentType = "application/json")
@ServiceConfig(protocol = Protocol.JMS, requestClass = Project.class, rolesAllowed = "Manager", endpoint = "queue:{scope}-update-project-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
public class UpdateProjectService extends AbstractProjectService implements
        RequestHandler {
    public UpdateProjectService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    @Override
    public void handle(Request request) {
        Project project = request.getPayload();
        ValidationException
                .builder()
                .assertNonNull(project.getId(), "undefined_id", "id",
                        "id not specified").end();

        Project saved = projectRepository.save(project);
        request.getResponseDispatcher().send(saved);
    }
}
