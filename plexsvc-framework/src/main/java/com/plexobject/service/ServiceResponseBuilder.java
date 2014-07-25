package com.plexobject.service;

public interface ServiceResponseBuilder {
    ServiceResponseBuilder setStatus(int status);

    ServiceResponseBuilder setContentType(String contentType);

    <R> ServiceResponseBuilder setReply(R reply);

    ServiceResponseBuilder setProperty(String name, String value);

    void send();
}
