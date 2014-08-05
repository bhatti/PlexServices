package com.plexobject.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionUtils {
    private static final String ERROR_TYPE = "errorType";

    private static final Logger log = LoggerFactory
            .getLogger(ExceptionUtils.class);

    private static final Set<String> FORBIDDEN_EXCEPTION_FIELDS = new HashSet<String>() {
        private static final long serialVersionUID = 1L;
        {
            add("class");
            add("stackTrace");
            add("cause");
            add("localizedMessage");
            add("suppressed");
        }
    };

    public static Map<String, Object> toMap(Exception e) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ERROR_TYPE, e.getClass().getSimpleName());
        try {
            BeanInfo info = Introspector.getBeanInfo(e.getClass());
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                Method reader = pd.getReadMethod();
                if (reader != null
                        && !FORBIDDEN_EXCEPTION_FIELDS.contains(pd.getName())) {
                    Object value = reader.invoke(e);
                    if (value != null) {
                        map.put(pd.getName(), value);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Failed to convert exception " + e, ex);
        }
        return map;
    }
}
