package com.plexobject.bugger.service.user;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Method;
import com.plexobject.service.ServiceException;

@ServiceConfig(requestClass = User.class, rolesAllowed = "Administrator", endpoint = "/users", method = Method.POST, contentType = "application/json")
public class CreateUserService extends AbstractUserService implements
        RequestHandler {
    public CreateUserService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void handle(Request request) {
        User user = request.getObject();
        ServiceException
                .builder()
                .addErrorIfNull(user, "undefined_user", "user",
                        "user not specified").raiseIfHasErrors();
        User saved = userRepository.save(user);
        request.getResponseBuilder().setReply(saved).send();
    }

}