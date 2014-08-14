package com.plexobject.bugger.service.user;

import java.util.HashMap;
import java.util.Map;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.GatewayType;
import com.plexobject.service.ServiceConfig.Method;

//@ServiceConfig(gateway = GatewayType.HTTP, requestClass = Void.class, rolesAllowed = "Administrator", endpoint = "/users/{id}/delete", method = Method.POST, codec = CodecType.JSON)
@ServiceConfig(gateway = GatewayType.JMS, requestClass = Void.class, rolesAllowed = "Administrator", endpoint = "queue:{scope}-delete-user-service-queue", method = Method.MESSAGE, codec = CodecType.JSON)
public class DeleteUserService extends AbstractUserService implements
        RequestHandler {
	public DeleteUserService(UserRepository userRepository) {
		super(userRepository);
	}

	@Override
	public void handle(Request request) {
		String id = request.getStringProperty("id");

		ValidationException.builder()
		        .assertNonNull(id, "undefined_id", "id", "id not specified")
		        .end();

		final boolean deleted = userRepository.delete(Long.valueOf(id));
		Map<String, Boolean> response = new HashMap<String, Boolean>() {
			private static final long serialVersionUID = 1L;

			{
				put("deleted", deleted);
			}
		};
		request.getResponseBuilder().send(response);
	}
}
