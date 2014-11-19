package com.plexobject.validation;

/**
 * This interface validates given fields against required field annotations
 * 
 * @author shahzad bhatti
 *
 */
public interface IRequiredFieldValidator {
    void validate(Object handler, Object request) throws ValidationException;

}