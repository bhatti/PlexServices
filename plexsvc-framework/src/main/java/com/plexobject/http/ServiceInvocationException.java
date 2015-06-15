package com.plexobject.http;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebFault;

import com.plexobject.domain.BaseException;

@XmlRootElement
@WebFault
public class ServiceInvocationException extends BaseException {
    private static final long serialVersionUID = 1L;

    public static class BuilderImpl extends BaseException.Builder {
        public ServiceInvocationException build() {
            return new ServiceInvocationException(message, cause, errors,
                    status);
        }
    }

    protected ServiceInvocationException() {
    }

    public ServiceInvocationException(String message, Throwable cause,
            final Collection<Error> errors, int status) {
        super(message, cause, errors, status);
    }

    public ServiceInvocationException(String message,
            final Collection<Error> errors, int status) {
        super(message, errors, status);
    }

    public ServiceInvocationException(String message, int status) {
        super(message, new HashSet<Error>(), status);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }
}
