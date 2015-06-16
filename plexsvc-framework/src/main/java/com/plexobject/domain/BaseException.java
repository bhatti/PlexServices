package com.plexobject.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.ws.WebFault;

@XmlRootElement
@WebFault
public abstract class BaseException extends RuntimeException implements
        Statusable {
    private static final long serialVersionUID = 1L;

    @XmlRootElement
    public static class Error {
        public final String errorCode;
        public final String fieldName;
        public final String errorMessage;

        public Error(String errorCode, String fieldName, String errorMessage) {
            this.errorCode = errorCode;
            this.fieldName = fieldName;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "Error [errorCode=" + errorCode + ", fieldName=" + fieldName
                    + ", errorMessage=" + errorMessage + "]";
        }
    }

    public static abstract class Builder {
        protected final Set<Error> errors = new HashSet<>();
        protected String message;
        protected Throwable cause;
        protected int status;

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder setCause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder addError(String errorCode, String fieldName,
                String errorMessage) {
            errors.add(new Error(errorCode, fieldName, errorMessage));
            return this;
        }

        public Builder assertNonNull(Object obj, String errorCode,
                String fieldName, String errorMessage) {
            if (obj == null) {
                errors.add(new Error(errorCode, fieldName, errorMessage));
            }
            return this;
        }

        public Builder assertNonEmpty(String buffer, String errorCode,
                String fieldName, String errorMessage) {
            if (buffer == null || buffer.length() == 0) {
                errors.add(new Error(errorCode, fieldName, errorMessage));
            }
            return this;
        }

        public Builder assertTrue(boolean predicate, String errorCode,
                String fieldName, String errorMessage) {
            if (!predicate) {
                errors.add(new Error(errorCode, fieldName, errorMessage));
            }
            return this;
        }

        public abstract BaseException build();

        public void end() {
            if (errors.size() > 0) {
                throw build();
            }
        }
    }

    private Collection<Error> errors;
    private int status;

    protected BaseException() {
    }

    public BaseException(String message, int status, final Error... errors) {
        super(message);
        this.errors = Arrays.asList(errors);
        this.status = status;
    }

    protected BaseException(String message, Throwable cause,
            final Collection<Error> errors, int status) {
        super(message, cause);
        this.errors = errors;
        this.status = status;
    }

    protected BaseException(String message, final Collection<Error> errors,
            int status) {
        super(message);
        this.errors = errors;
        this.status = status;
    }

    public Collection<Error> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [errors=" + errors + ", super "
                + super.toString() + "]";
    }

    @Override
    public int getStatus() {
        return status;
    }

}
