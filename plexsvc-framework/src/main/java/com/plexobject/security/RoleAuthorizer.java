package com.plexobject.security;

import com.plexobject.handler.Request;

public interface RoleAuthorizer {
    boolean hasRole(Request request, String role);
}
