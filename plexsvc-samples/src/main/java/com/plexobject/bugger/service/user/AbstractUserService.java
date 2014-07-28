package com.plexobject.bugger.service.user;

import com.plexobject.bugger.repository.UserRepository;
import com.plexobject.bugger.service.AbstractService;

public class AbstractUserService extends AbstractService {
    public AbstractUserService(final UserRepository userRepository) {
        super(userRepository);
    }
}
