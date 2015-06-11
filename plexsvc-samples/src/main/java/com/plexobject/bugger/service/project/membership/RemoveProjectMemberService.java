package com.plexobject.bugger.service.project.membership;

import com.plexobject.bugger.model.Project;
import com.plexobject.bugger.repository.ProjectRepository;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.project.AbstractProjectService;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Method;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;
import com.plexobject.validation.ValidationException;

//@ServiceConfig(protocol = Protocol.HTTP, rolesAllowed = "Manager", endpoint = "/projects/{id}/membership/remove", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Manager", endpoint = "queue://{scope}-remove-project-member-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "projectId"), @Field(name = "assignedTo") })
public class RemoveProjectMemberService extends AbstractProjectService
        implements RequestHandler {
    public RemoveProjectMemberService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    @Override
    public void handle(Request<Object> request) {
        String projectId = request.getProperty("id");
        String assignedTo = request.getProperty("assignedTo");
        boolean projectLead = "true".equals(request.getProperty("projectLead"));

        Project project = projectRepository.load(Long.valueOf(projectId));
        ValidationException
                .builder()
                .assertNonNull(project, "projectNotFound", "project",
                        "project not found").end();
        if (projectLead) {
            project.setProjectLead(null);
        } else {
            project.removeMember(assignedTo);
        }
        request.getResponse().setPayload(project);
    }
}
