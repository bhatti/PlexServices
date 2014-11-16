package com.plexobject.bugger.service.user;

import com.plexobject.bugger.model.User;
import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(protocol = Protocol.HTTP, requestClass = User.class, rolesAllowed = "Administrator", endpoint = "/users", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(protocol = Protocol.JMS, requestClass = User.class, rolesAllowed = "Administrator", endpoint = "queue:{scope}-create-user-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
public class CreateUserService extends AbstractUserService implements
        RequestHandler {
	public CreateUserService(UserRepository userRepository) {
		super(userRepository);
	}

	@Override
	public void handle(Request request) {
		User user = request.getPayload();
		user.validate();
		User saved = userRepository.save(user);
		request.getResponseDispatcher().send(saved);
	}

}
