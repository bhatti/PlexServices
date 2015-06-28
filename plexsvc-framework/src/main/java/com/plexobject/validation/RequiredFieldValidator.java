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
    public void validate(Object request, Map<String, Object> properties)
            throws ValidationException {
        validate(request, request, properties);
    }

    @Override
    public void validate(Object handler, Object request,
            Map<String, Object> properties) throws ValidationException {
        if (handler == null) {
            return;
        }
        RequiredFields requiredFields = handler.getClass().getAnnotation(
                RequiredFields.class);
        validate(requiredFields, request, properties);
    }

    @Override
    public void validate(RequiredFields requiredFields, Object request,
            Map<String, Object> properties) throws ValidationException {
        if (requiredFields == null) {
            return;
        }
        Field[] requiredFieldsValue = requiredFields.value();

        ValidationException.Builder validationExceptionBuilder = ValidationException
                .builder();
        for (Field f : requiredFieldsValue) {
            try {
                String val = getProperty(request, properties, f.name());
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

    @SuppressWarnings("unchecked")
    private static String getProperty(Object request,
            Map<String, Object> properties, String name)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Object value = null;
        if (properties != null) {
            value = properties.get(name);
        }
        if (value == null && request != null) {
            if (request instanceof Map) {
                value = ((Map<String, Object>) request).get(name);
            } else {
                value = BeanUtils.getProperty(request, name);
            }
        }
        if (value == null) {
            return null;
        }
        return value instanceof String ? (String) value : value.toString();
    }
}
