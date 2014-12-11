package com.plexobject.validation;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

/**
 * This class validates given fields against required field annotations
 * 
 * @author shahzad bhatti
 *
 */
public class RequiredFieldValidator implements IRequiredFieldValidator {
    @Override
    public void validate(Object request) throws ValidationException {
        validate(request, request);
    }

    @Override
    public void validate(Object handler, Object request)
            throws ValidationException {
        if (handler == null) {
            return;
        }
        RequiredFields requiredFields = handler.getClass().getAnnotation(
                RequiredFields.class);
        validate(requiredFields, request);
    }

    @Override
    public void validate(RequiredFields requiredFields, Object request)
            throws ValidationException {
        if (requiredFields == null) {
            return;
        }
        Field[] requiredFieldsValue = requiredFields.value();

        ValidationException.Builder validationExceptionBuilder = ValidationException
                .builder();
        for (Field f : requiredFieldsValue) {
            try {
                String val = getProperty(request, f.name());
                if (val == null) {
                    validationExceptionBuilder.addError(
                            "undefined_" + f.name(), f.name(), f.name()
                                    + " is not defined");
                    continue;
                }
                if (f.regex().length() > 0) {
                    if (!val.matches(f.regex())) {
                        validationExceptionBuilder.addError(
                                "invalid_" + f.name(),
                                f.name(),
                                "The value '" + val + "' for " + f.name()
                                        + " does not match pattern "
                                        + f.regex());
                    }
                }

                if (val.length() < f.minLength()) {
                    validationExceptionBuilder.addError("invalid_" + f.name(),
                            f.name(), "The value '" + val
                                    + "' is too short for " + f.name());
                }
                if (val.length() > f.maxLength()) {
                    validationExceptionBuilder.addError("invalid_" + f.name(),
                            f.name(), "The value '" + val
                                    + "' is too long for " + f.name());
                }
            } catch (Exception e) {
                validationExceptionBuilder.addError("undefined_" + f.name(),
                        f.name(), f.name() + " is not defined");
            }
        }
        validationExceptionBuilder.end();
    }

    private static String getProperty(Object request, String name)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        if (request instanceof Map) {
            @SuppressWarnings("unchecked")
            Object val = ((Map<String, Object>) request).get(name);
            if (val == null) {
                return null;
            }
            return val instanceof String ? (String) val : val.toString();
        } else {
            return BeanUtils.getProperty(request, name);
        }
    }

}
