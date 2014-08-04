package com.plexobject.service;

import com.plexobject.domain.ValidationException;


public class ErrorResponse {
    private Object payload;

    public static class Error {
        private String errorType;
        private String errorMessage;

        public Error() {

        }

        public Error(String errorType, String errorMessage) {
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
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
        } else {
            setPayload(new Error(e.getClass().getSimpleName(), e.toString()));
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
