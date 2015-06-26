package com.plexobject.bugger.service.user;

import java.util.HashMap;
import java.util.Map;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

@ServiceConfig(protocol = Protocol.JMS, rolesAllowed = "Administrator", endpoint = "queue://{scope}-delete-user-service-queue", method = RequestMethod.MESSAGE, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "id") })
public class DeleteUserService extends AbstractUserService implements
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
        request.getResponse().setPayload(response);
    }
}
