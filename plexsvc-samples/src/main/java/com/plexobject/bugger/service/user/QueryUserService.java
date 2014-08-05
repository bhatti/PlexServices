package com.plexobject.bugger.service.user;

import java.util.Collection;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.predicate.Predicate;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = User.class, rolesAllowed = "Administrator", endpoint = "/users", method = Method.GET, contentType = "application/json")
@ServiceConfig(gateway = GatewayType.JMS, requestClass = User.class, rolesAllowed = "Administrator", endpoint = "queue:{scope}-query-user-service-queue", method = Method.LISTEN, contentType = "application/json")
public class QueryUserService extends AbstractUserService implements
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
        Collection<User> users = userRepository.getAll(new Predicate<User>() {

            @Override
            public boolean accept(User u) {
                return true;
            }
        });
        request.getResponseBuilder().send(users);
    }
}
