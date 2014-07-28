package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = Project.class, rolesAllowed = "Manager", endpoint = "/projects/{id}", method = Method.POST, contentType = "application/json")
public class UpdateProjectService extends AbstractProjectService implements
        RequestHandler {
    public UpdateProjectService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    @Override
    public void handle(Request request) {
        String id = request.getProperty("id");

        Project project = request.getObject();
        ServiceException
                .builder()
                .addErrorIfNull(id, "undefined_id", "id", "id not specified")
                .addErrorIfNull(project, "undefined_project", "project",
                        "project not specified").raiseIfHasErrors();
        project.setId(Long.valueOf(id));

        Project saved = projectRepository.save(project);
        request.getResponseBuilder().setReply(saved).send();
    }
}