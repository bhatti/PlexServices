package com.plexobject.service;

import com.plexobject.domain.AuthException;
import com.plexobject.domain.ValidationException;

public class ErrorResponse {
    private Object payload;

    public static class Error {
        private String status;
        private String location;
        private String errorMessage;

        public Error() {

        }

        public Error(String status, String location, String errorMessage) {
            this.status = status;
            this.location = location;
            this.errorMessage = errorMessage;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public ErrorResponse() {

    }

    public ErrorResponse(Object payload) {
        setPayload(payload);
    }

    public ErrorResponse(Exception e) {
        if (e instanceof ValidationException) {
            setPayload(((ValidationException) e).getErrors());
        } else if (e instanceof AuthException) {
            AuthException authException = (AuthException) e;
            setPayload(new Error(authException.getStatus(),
                    authException.getLocation(), e.toString()));
        } else {
            setPayload(new Error("501", "", e.toString()));
        }

        setPayload(payload);
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
