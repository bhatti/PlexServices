package com.plexobject.bugger.service.user;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.Constants;
import com.plexobject.domain.ValidationException;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.ResponseBuilder;
import com.plexobject.security.AuthException;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, endpoint = "/login", method = Method.POST, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Void.class, endpoint = "queue:{scope}-login-service-queue", method = Method.LISTEN, contentType = "application/json")
public class LoginService extends AbstractUserService implements RequestHandler {
    public LoginService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void handle(Request request) {
        String username = request.getStringProperty("username");
        String password = request.getStringProperty("password");

        ValidationException
                .builder()
                .assertNonNull(username, "undefined_username", "username",
                        "username not specified")
                .assertNonNull(password, "undefined_password", "password",
                        "password not specified").end();

        User user = userRepository.authenticate(username, password);
        ResponseBuilder responseBuilder = request.getResponseBuilder();
        if (user == null) {
            throw new AuthException(Constants.SC_UNAUTHORIZED,
                    "/login?errorCode=authenticationFailed",
                    "failed to authenticate");
        } else {
            responseBuilder.addSessionId(userRepository.getSessionId(user));
            responseBuilder.send(user);
        }
    }
}
