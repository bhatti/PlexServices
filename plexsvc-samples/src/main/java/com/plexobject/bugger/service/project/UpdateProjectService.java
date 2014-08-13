package com.plexobject.bugger.service.project;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP,requestClass = Project.class, rolesAllowed = "Manager", endpoint = "/projects/{id}", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Project.class, rolesAllowed = "Manager", endpoint = "queue:{scope}-update-project-service-queue", method = Method.MESSAGE, codec = CodecType.BINARY)
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
		request.getResponseBuilder().send(saved);
	}
}
