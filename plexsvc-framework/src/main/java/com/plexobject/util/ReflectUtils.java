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
    public static Object[] decode(Method m,
            Pair<String, String>[] paramNamesAndDefaults,
            Map<String, Object> props) throws Exception {
        Pair<Class, Type>[] pairs = new Pair[m.getParameterTypes().length];
        for (int i = 0; i < m.getParameterTypes().length; i++) {
            pairs[i] = Pair.of((Class) m.getParameterTypes()[i],
                    m.getGenericParameterTypes()[i]);
        }
        return decode(pairs, paramNamesAndDefaults, props);
    }

    @SuppressWarnings("rawtypes")
    public static Object[] decode(Pair<Class, Type>[] types,
            Pair<String, String>[] paramNamesAndDefaults,
            Map<String, Object> props) throws Exception {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> klass = types[i].first;
            Object value = props.get(paramNamesAndDefaults[i].first);
            if (value == null) {
                value = paramNamesAndDefaults[i].second;
            }
            args[i] = decodePrimitive(value, klass);
        }
        return args;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Object[] decode(String payload, Method m, ObjectCodec codec)
            throws Exception {
        Pair<Class, Type>[] pairs = new Pair[m.getParameterTypes().length];
        for (int i = 0; i < m.getParameterTypes().length; i++) {
            pairs[i] = Pair.of((Class) m.getParameterTypes()[i],
                    m.getGenericParameterTypes()[i]);
        }
        return decode(payload, pairs, codec);
    }

    @SuppressWarnings("rawtypes")
    public static Object[] decode(String payload, Pair<Class, Type>[] params,
            ObjectCodec codec) throws Exception {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> klass = params[i].first;
            Type pKlass = params[i].second;
            args[i] = decode(payload, klass, pKlass, codec);
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
