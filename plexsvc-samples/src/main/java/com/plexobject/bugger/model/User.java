package com.plexobject.bugger.model;

import java.util.Collection;
import java.util.HashSet;

import com.plexobject.security.Authorizable;
import com.plexobject.security.AuthorizableRole;

public class User implements Authorizable {
    private String username;
    private Collection<AuthorizableRole> roles = new HashSet<AuthorizableRole>();

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<AuthorizableRole> getRoles() {
        return roles;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(Collection<AuthorizableRole> roles) {
        this.roles = roles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "User [username=" + username + ", roles=" + roles + "]";
    }

}
