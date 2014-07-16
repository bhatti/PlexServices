package com.plexobject.security;


public interface Authorizable {
    String getUsername();

    boolean hasRole(String role);
}
