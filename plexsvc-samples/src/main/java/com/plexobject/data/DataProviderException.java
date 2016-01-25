package com.plexobject.data;

/**
 * This exception is thrown when an error occurs in the data provider
 * 
 * @author shahzad bhatti
 *
 */
public class DataProviderException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataProviderException(String message) {
        super(message);
    }

}
