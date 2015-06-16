package com.plexobject.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.plexobject.domain.BaseException;

/**
 * This class provides helper methods for exceptions s *
 * 
 * @author shahzad bhatti
 *
 */
public class ExceptionUtils {
    private static final Logger log = Logger.getLogger(ExceptionUtils.class);
    private static final String ERRORS = "errors";
    private static final String ERROR_TYPE = "errorType";

    private static final Set<String> FORBIDDEN_EXCEPTION_FIELDS = new HashSet<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("class");
            add("stackTrace");
            add("cause");
            add("localizedMessage");
            add("suppressed");
            add("targetException");
        }
    };

    public static Map<String, Object> toErrors(Exception e) {
        if (e.getCause() != null) {
            e = (Exception) e.getCause();
        }
        Map<String, Object> errorsMap = new HashMap<String, Object>();
        if (e instanceof BaseException) {
            errorsMap.put(ERROR_TYPE, e.getClass().getSimpleName());
            addErrors(e, errorsMap);
        } else {
            List<Map<String, Object>> errors = new ArrayList<>();
            Map<String, Object> nestedErrorsMap = new HashMap<String, Object>();
            errors.add(nestedErrorsMap);
            errorsMap.put(ERRORS, errors);
            nestedErrorsMap.put(ERROR_TYPE, e.getClass().getSimpleName());
            addErrors(e, nestedErrorsMap);
        }

        return errorsMap;
    }

    private static void addErrors(Exception e, Map<String, Object> errorsMap) {
        try {
            BeanInfo info = Introspector.getBeanInfo(e.getClass());
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader != null
                        && !FORBIDDEN_EXCEPTION_FIELDS.contains(pd.getName())) {
                    Object value = reader.invoke(e);
                    if (value != null) {
                        errorsMap.put(pd.getName(), value);
                    }
                }
            }
            if (e.getCause() instanceof Exception) {
                Exception root = (Exception) e.getCause();
                addErrors(root, errorsMap);
            }
        } catch (Exception ex) {
            e.printStackTrace();
            log.error("Failed to convert exception " + e, ex);
        }
    }
}
