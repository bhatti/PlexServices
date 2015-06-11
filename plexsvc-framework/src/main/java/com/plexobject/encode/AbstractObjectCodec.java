package com.plexobject.encode;

import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.log4j.Logger;

/**
 * This is base class for encoding implementation classes that provides helper
 * methods
 * 
 * @author shahzad bhatti
 *
 */
public abstract class AbstractObjectCodec implements ObjectCodec {
    protected final Logger log = Logger.getLogger(getClass());
    private static final BeanUtilsBean beanUtilsBean = new BeanUtilsBean(
            new ConvertUtilsBean() {
                @SuppressWarnings("unchecked")
                @Override
                public Object convert(String value,
                        @SuppressWarnings("rawtypes") Class clazz) {
                    if (clazz.isEnum()) {
                        return Enum.valueOf(clazz, value.toUpperCase());
                    } else {
                        return super.convert(value, clazz);
                    }
                }
            });

    @SuppressWarnings("unchecked")
    protected <T> T propertyDecode(Map<String, Object> params, Class<?> type) {
        try {
            T object = (T) type.newInstance();
            populateProperties(params, object);
            return object;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EncodingException("Failed to decode " + params, ex);
        }
    }

    protected <T> void populateProperties(Map<String, Object> params, T object) {
        if (params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                try {
                    String name = toCamelCase(e.getKey());
                    beanUtilsBean.setProperty(object, name, e.getValue());
                } catch (Exception ex) {
                    log.warn("Failed to set \"" + e.getKey()
                            + "\" with value \"" + e.getValue() + "\"", ex);
                }
            }
        }
    }

    protected static String toCamelCase(String s) {
        String[] parts = s.split("_");
        if (parts.length < 2) {
            return s;
        }
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            sb.append(toProperCase(parts[i]));
        }
        return sb.toString();
    }

    protected static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

}
