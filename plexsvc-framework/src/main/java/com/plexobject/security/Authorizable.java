package com.plexobject.security;

import java.util.Collection;

public interface Authorizable {
    String getUsername();

    Collection<AuthorizableRole> getRoles();
}
