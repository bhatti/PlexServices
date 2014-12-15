package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

//@ServiceConfig(protocol = Protocol.HTTP,payloadClass = Project.class, rolesAllowed = "Manager", endpoint = "/projects/{id}", method = Method.POST, contentType = "application/json")
@ServiceConfig(protocol = Protocol.JMS, payloadClass = Project.class, rolesAllowed = "Manager", endpoint = "queue://{scope}-update-project-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "id") })
public class UpdateProjectService extends AbstractProjectService implements
        RequestHandler {
    public UpdateProjectService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    @Override
    public void handle(Request request) {
        Project project = request.getPayload();
        Project saved = projectRepository.save(project);
        request.getResponseDispatcher().send(saved);
    }
}
