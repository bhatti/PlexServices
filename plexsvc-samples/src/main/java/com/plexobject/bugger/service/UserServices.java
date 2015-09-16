package com.plexobject.bugger.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.security.AuthException;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

public class UserServices {
    public static class AbstractUserService extends AbstractService {
        public AbstractUserService(final UserRepository userRepository) {
            super(userRepository);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, contentsClass = User.class, rolesAllowed = "Administrator", endpoint = "queue://{scope}-create-user-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "username") })
    public static class CreateUserService extends AbstractUserService implements
            RequestHandler {
        public CreateUserService(UserRepository userRepository) {
            super(userRepository);
        }

        @Override
        public void handle(Request request) {
            User user = request.getContentsAs();
            User saved = userRepository.save(user);
            request.getResponse().setContents(saved);
        }

    }

    @ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Administrator", endpoint = "queue://{scope}-delete-user-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "id") })
    public static class DeleteUserService extends AbstractUserService implements
            RequestHandler {
        public DeleteUserService(UserRepository userRepository) {
            super(userRepository);
        }

        @Override
        public void handle(Request request) {
            String id = request.getStringProperty("id");
            final boolean deleted = userRepository.delete(Long.valueOf(id));
            Map<String, Boolean> response = new HashMap<String, Boolean>() {
                private static final long serialVersionUID = 1L;

                {
                    put("deleted", deleted);
                }
            };
            request.getResponse().setContents(response);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, endpoint = "queue://{scope}-login-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "username"), @Field(name = "password") })
    public static class LoginService extends AbstractUserService implements
            RequestHandler {
        public LoginService(UserRepository userRepository) {
            super(userRepository);
        }

        @Override
        public void handle(Request request) {
            log.info("PAYLOAD " + request.getContentsAs());
            String username = request.getStringProperty("username");
            String password = request.getStringProperty("password");

            User user = userRepository.authenticate(username, password);
            if (user == null) {
                throw new AuthException("authError", "failed to authenticate");
            } else {
                request.getResponse().addSessionId(
                        userRepository.getSessionId(user));
                request.getResponse().setContents(user);
            }
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, contentsClass = User.class, rolesAllowed = "Administrator", endpoint = "queue://{scope}-query-user-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    public static class QueryUserService extends AbstractUserService implements
            RequestHandler {
        public QueryUserService(UserRepository userRepository) {
            super(userRepository);
        }

        // [{"id":1,"username":"alex","password":"pass","email":"alex@plexobject.com",
        // "roles":["Employee"]},{"id":2,"username":"jeff","password":"pass",
        // "email":"jeff@plexobject.com","roles":["Employee","Manager"]},{"id":3,"username":"scott",
        // "password":"pass","email":"scott@plexobject.com",
        // "roles":["Employee","Administrator","Manager"]},{"id":4,"username":"erica","password":
        // "pass","email":"erica@plexobject.com","roles":["Employee"]}]
        @Override
        public void handle(Request request) {
            Collection<User> users = userRepository
                    .getAll(new Predicate<User>() {

                        @Override
                        public boolean accept(User u) {
                            return true;
                        }
                    });
            request.getResponse().setContents(users);
        }
    }

    @ServiceConfig(protocol = Protocol.JMS, contentsClass = User.class, rolesAllowed = "Administrator", endpoint = "queue://{scope}-update-user-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
    @RequiredFields({ @Field(name = "id") })
    public static class UpdateUserService extends AbstractUserService implements
            RequestHandler {
        public UpdateUserService(UserRepository userRepository) {
            super(userRepository);
        }

        @Override
        public void handle(Request request) {
            User user = request.getContentsAs();

            User saved = userRepository.save(user);
            request.getResponse().setContents(saved);
        }
    }
}
