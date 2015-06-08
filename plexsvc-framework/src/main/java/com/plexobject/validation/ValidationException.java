package com.plexobject.validation;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import com.plexobject.domain.BaseException;
import com.plexobject.http.HttpResponse;

/**
 * This exception is raised when validation error occurs
 * 
 * @author shahzad bhatti
 *
 */
@XmlRootElement
public class ValidationException extends BaseException {
    private static final long serialVersionUID = 1L;

    public static class BuilderImpl extends BaseException.Builder {
        public ValidationException build() {
            return new ValidationException(message, cause, errors);
        }
    }

    protected ValidationException() {
    }

    public ValidationException(String message, Throwable cause,
            final Collection<Error> errors) {
        super(message, cause, errors, HttpResponse.SC_BAD_REQUEST);
    }

    public ValidationException(String message, final Collection<Error> errors) {
        super(message, errors, HttpResponse.SC_BAD_REQUEST);
    }

    public static Builder builder() {
        return new BuilderImpl();
    }
}
