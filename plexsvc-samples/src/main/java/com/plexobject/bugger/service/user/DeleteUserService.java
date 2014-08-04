package com.plexobject.bugger.service.user;

import java.util.HashMap;
import java.util.Map;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, rolesAllowed = "Administrator", endpoint = "/users/{id}/delete", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Void.class, rolesAllowed = "Administrator", endpoint = "queue:{scope}-delete-user-service-queue", method = Method.LISTEN, contentType = "application/json")
public class DeleteUserService extends AbstractUserService implements
        RequestHandler {
    public DeleteUserService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void handle(Request request) {
        String id = request.getProperty("id");

        ValidationException.builder()
                .assertNonNull(id, "undefined_id", "id", "id not specified")
                .end();

        boolean deleted = userRepository.delete(Long.valueOf(id));
        Map<String, Boolean> response = new HashMap<String, Boolean>() {
            {
                put("deleted", deleted);
            }
        };
        request.getResponseBuilder().sendSuccess(response);
    }
}
