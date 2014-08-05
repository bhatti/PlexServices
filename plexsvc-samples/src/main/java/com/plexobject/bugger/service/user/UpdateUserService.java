package com.plexobject.bugger.service.user;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = User.class, rolesAllowed = "Administrator", endpoint = "/users/{id}", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = User.class, rolesAllowed = "Administrator", endpoint = "queue:{scope}-update-user-service-queue", method = Method.LISTEN, contentType = "application/json")
public class UpdateUserService extends AbstractUserService implements
        RequestHandler {
    public UpdateUserService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void handle(Request request) {
        User user = request.getPayload();
        ValidationException
                .builder()
                .assertNonNull(user.getId(), "undefined_id", "id",
                        "id not specified").end();

        User saved = userRepository.save(user);
        request.getResponseBuilder().send(saved);
    }
}
