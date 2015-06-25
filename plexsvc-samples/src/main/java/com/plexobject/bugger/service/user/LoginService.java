package com.plexobject.bugger.service.user;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.security.AuthException;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

@ServiceConfig(protocol = Protocol.JMS, endpoint = "queue://{scope}-login-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "username"), @Field(name = "password") })
public class LoginService extends AbstractUserService implements RequestHandler {
    public LoginService(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public void handle(Request<Object> request) {
        log.info("PAYLOAD " + request.getPayload());
        String username = request.getStringProperty("username");
        String password = request.getStringProperty("password");

        User user = userRepository.authenticate(username, password);
        if (user == null) {
            throw new AuthException("authError",
                    "failed to authenticate");
        } else {
            request.getResponse().addSessionId(
                    userRepository.getSessionId(user));
            request.getResponse().setPayload(user);
        }
    }
}
