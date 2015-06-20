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

//@ServiceConfig(protocol = Protocol.HTTP, rolesAllowed = "Manager", endpoint = "/projects/{id}/membership/add", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Manager", endpoint = "queue://{scope}-add-project-member-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "assignedTo"), @Field(name = "projectId") })
public class AddProjectMemberService extends AbstractProjectService implements
        RequestHandler {
    public AddProjectMemberService(ProjectRepository projectRepository,
            UserRepository userRepository) {
        super(projectRepository, userRepository);
    }

    @Override
    public void handle(Request<Object> request) {
        String projectId = request.getStringProperty("id");
        String assignedTo = request.getStringProperty("assignedTo");
        boolean projectLead = "true".equals(request
                .getStringProperty("projectLead"));
        Project project = projectRepository.load(Long.valueOf(projectId));
        ValidationException
                .builder()
                .assertNonNull(project, "projectNotFound", "project",
                        "project not found").end();
        if (projectLead) {
            project.setProjectLead(assignedTo);
        } else {
            project.addMember(assignedTo);
        }
        request.getResponse().setPayload(project);
    }
}
