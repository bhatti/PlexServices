package com.plexobject.bugger.model;

import java.util.Collection;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plexobject.domain.Validatable;
import com.plexobject.domain.ValidationException;

public class User implements Validatable {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Collection<String> roles = new HashSet<>();

    public User() {

    }

    public User(String username, String password, String email, String... roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        for (String role : roles) {
            this.roles.add(role);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public void removeRole(String role) {
        this.roles.remove(role);
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        return "User [username=" + username + ", email=" + email + ", roles="
                + roles + "]";
    }

    @Override
    public void validate() throws ValidationException {
        ValidationException
                .builder()
                .addErrorIfNull(username, "undefined_username", "username",
                        "username not specified").raiseIfHasErrors();
    }

}
