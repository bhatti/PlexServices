package com.plexobject.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;

import org.reflections.Reflections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.domain.Pair;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.json.JsonObjectCodec;

public class ReflectUtils {
    public enum ParamType {
        QUERY_FORM_PARAM, JSON_PARAM, REQUEST_PARAM, MAP_PARAM, UNKNOWN_PARAM
    }

    public static class Param {
        public final String name;
        public final ParamType type;
        public Object defaultValue;

        public Param(ParamType type, String name, Object defaultValue) {
            this.type = type;
            this.name = name;
            this.defaultValue = defaultValue;
        }
    }

    public static Collection<Class<?>> getAnnotatedClasses(
            Class<? extends Annotation> klass, String... pkgNames) {
        Set<Class<?>> serviceClasses = new HashSet<>();
        for (String pkgName : pkgNames) {
            pkgName = pkgName.trim();
            if (pkgName.length() == 0) {
                continue;
            }
            Reflections reflections = new Reflections(pkgName);
            serviceClasses.addAll(reflections.getTypesAnnotatedWith(klass));
        }
        return serviceClasses;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object[] decode(Method m, Map<String, Object> props,
            Param[] params, String payload, ObjectCodec codec) throws Exception {
        Pair<Class, Type>[] types = new Pair[m.getParameterTypes().length];
        if (params == null) {
            params = new Param[types.length];
        }
        for (int i = 0; i < m.getParameterTypes().length; i++) {
            types[i] = Pair.of((Class) m.getParameterTypes()[i],
                    m.getGenericParameterTypes()[i]);
            if (params[i] == null) {
                params[i] = new Param(ParamType.JSON_PARAM, null, null);
            }
        }
        return decode(types, props, params, payload, codec);
    }

    @SuppressWarnings("rawtypes")
    public static Object[] decode(Pair<Class, Type>[] types,
            Map<String, Object> props, Param[] params, String payload,
            ObjectCodec codec) throws Exception {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> klass = types[i].first;
            Type pKlass = types[i].second;

            switch (params[i].type) {
            case QUERY_FORM_PARAM:
                Object value = props.get(params[i].name);
                if (value == null) {
                    value = params[i].defaultValue;
                }
                args[i] = decodePrimitive(value, klass);
                break;
            case JSON_PARAM:
                args[i] = decode(payload, klass, pKlass, codec);
                break;
            case MAP_PARAM: // TODO this can be improved to make sure we pass
                            // header/properties properly if payload is given
                            // and when multiple parameters are passed
                if (payload != null && payload.length() > 0) {
                    args[i] = decode(payload, klass, pKlass, codec);
                } else {
                    args[i] = params[i].defaultValue;
                }
                break;
            case REQUEST_PARAM:
                args[i] = params[i].defaultValue;
                break;
            default:
                break;
            }
        }
        return args;
    }

    public static Object decode(String payload, Class<?> klass, Type pKlass,
            ObjectCodec codec) throws Exception {
        if ("null".equals(payload)) {
            return null;
        }
        if (klass == Void.class) {
            return null;
        } else if (klass.isPrimitive()) {
            return decodePrimitive(payload, klass);
        } else if (CharSequence.class.isAssignableFrom(klass)) {
            return decodeString(payload, klass);
        } else {
            return decodeObject(payload, klass, pKlass, codec);
        }
    }

    public static Class<?> getWebServiceInterface(Class<?> serviceClass) {
        Class<?>[] interfaces = serviceClass.getInterfaces();
        for (Class<?> iface : interfaces) {
            WebService webService = iface.getAnnotation(WebService.class);
            if (webService != null) {
                return iface;
            }
        }
        return null;
    }

    private static Object decodeObject(final String payload,
            final Class<?> klass, final Type pKlass, final ObjectCodec codec)
            throws Exception {
        if (pKlass != null && codec instanceof JsonObjectCodec) {
            return ((JsonObjectCodec) codec).decode(payload,
                    new TypeReference<Object>() {
                        public Type getType() {
                            return pKlass;
                        }
                    });
        } else {
            return codec.decode(payload, klass, null);
        }
    }

    private static Object decodeString(Object payload, Class<?> klass) {
        return payload.toString();
    }

    private static Object decodePrimitive(Object payload, Class<?> klass) {
        if (payload == null) {
            return null;
        }
        if (klass == String.class && payload instanceof String) {
            return (String) payload;
        }
        String text = payload.toString();
        if (klass == Byte.class || klass == Byte.TYPE) {
            return Byte.valueOf(text);
        } else if (klass == Boolean.class || klass == Boolean.TYPE) {
            return Boolean.valueOf(text);
        } else if (klass == Character.class || klass == Character.TYPE) {
            return text.charAt(0);
        } else if (klass == Short.class || klass == Short.TYPE) {
            return Short.valueOf(text);
        } else if (klass == Integer.class || klass == Integer.TYPE) {
            return Integer.valueOf(text);
        } else if (klass == Long.class || klass == Long.TYPE) {
            return Long.valueOf(text);
        } else if (klass == Float.class || klass == Float.TYPE) {
            return Float.valueOf(text);
        } else if (klass == Double.class || klass == Double.TYPE) {
            return Double.valueOf(text);
        } else if (klass == String.class) {
            return text;
        }
        throw new IllegalArgumentException("Could not convert " + text + " to "
                + klass.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getMethodParameterAnnotation(final Method iMethod,
            Class<?> type) {
        for (Annotation[] annotations : iMethod.getParameterAnnotations()) {
            for (Annotation a : annotations) {
                if (type.isAssignableFrom(a.getClass())) {
                    return (T) a;
                }
            }
        }
        return null;
    }
}
