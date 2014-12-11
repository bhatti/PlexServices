package com.plexobject.validation;

/**
 * This interface validates given fields against required field annotations
 * 
 * @author shahzad bhatti
 *
 */
public interface IRequiredFieldValidator {
    /**
     * This method retrieves RequiredFields annotations from handler objects and
     * validates request object
     * 
     * @param handler
     *            - object that defines RequiredFields annotations
     * @param request
     *            - incoming request
     * @throws ValidationException
     */
    void validate(Object handler, Object request) throws ValidationException;

    /**
     * This method validates request object against required fields
     * 
     * @param requiredFields
     *            - annotations that define required fields
     * @param request
     *            - incoming request
     * @throws ValidationException
     */
    void validate(RequiredFields requiredFields, Object request)
            throws ValidationException;

    /**
     * This method checks if request object implements RequiredFields and if so
     * then it validates the object against annotation rules
     * 
     * @param request
     * @throws ValidationException
     */
    void validate(Object request) throws ValidationException;

}