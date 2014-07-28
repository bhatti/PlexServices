package com.plexobject.bugger.service.user;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = Void.class, rolesAllowed = "Administrator", endpoint = "/users/{id}", method = Method.POST, contentType = "application/json")
public class DeleteUserService extends AbstractUserService implements
        RequestHandler {
    public DeleteUserService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void handle(Request request) {
        String id = request.getProperty("id");

        ServiceException.builder()
                .addErrorIfNull(id, "undefined_id", "id", "id not specified")
                .raiseIfHasErrors();

        userRepository.delete(Long.valueOf(id));
        request.getResponseBuilder().send();
    }
}