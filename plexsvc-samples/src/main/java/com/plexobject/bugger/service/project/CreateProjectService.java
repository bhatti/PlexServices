package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

@ServiceConfig(protocol = Protocol.JMS, payloadClass = Project.class, rolesAllowed = "Manager", endpoint = "queue://{scope}-create-projects-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "projectId") })
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
        Project saved = projectRepository.save(project);
        request.getResponse().setPayload(saved);
    }

}
