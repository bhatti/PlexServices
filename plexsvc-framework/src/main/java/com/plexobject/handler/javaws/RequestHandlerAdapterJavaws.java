package com.plexobject.handler.javaws;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Path;

import com.plexobject.domain.Configuration;
import com.plexobject.domain.Pair;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.RequestHandlerAdapter;
import com.plexobject.service.Protocol;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.util.ReflectUtils;

public class RequestHandlerAdapterJavaws implements RequestHandlerAdapter {
    private static final String DEFAULT_VERSION = "1.0";
    private final Configuration config;

    public RequestHandlerAdapterJavaws(Configuration config) {
        this.config = config;
    }

    public Map<ServiceConfigDesc, RequestHandler> createFromPackages(
            String... pkgNames) {
        Collection<Class<?>> serviceClasses = ReflectUtils.getAnnotatedClasses(
                WebService.class, pkgNames);
        Map<ServiceConfigDesc, RequestHandler> handlers = new HashMap<>();
        for (Class<?> serviceClass : serviceClasses) {
            if (!serviceClass.isInterface()) {
                Class<?> webService = getWebServiceInterface(serviceClass);
                if (webService == null) {
                    continue;
                }
                try {
                    Pair<ServiceConfigDesc, RequestHandler> handlerAndConfig = create(
                            serviceClass, (ServiceConfigDesc) null);
                    handlers.put(handlerAndConfig.first,
                            handlerAndConfig.second);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
        return handlers;
    }

    @Override
    public Pair<ServiceConfigDesc, RequestHandler> create(
            Class<?> serviceClass, ServiceConfigDesc serviceConfig) {
        try {
            Object service = serviceClass.newInstance();
            return create(service, serviceConfig);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pair<ServiceConfigDesc, RequestHandler> create(Object service,
            String endpoint) {
        ServiceConfigDesc serviceConfig = endpoint != null ? new ServiceConfigDesc(
                Protocol.HTTP, com.plexobject.service.Method.POST, Void.class,
                config.getDefaultCodecType(), DEFAULT_VERSION, endpoint, true,
                new String[0], 1) : null;
        return create(service, serviceConfig);
    }

    @Override
    public Pair<ServiceConfigDesc, RequestHandler> create(Object service,
            ServiceConfigDesc serviceConfig) {
        Class<?> serviceClass = service.getClass();
        Class<?> webService = getWebServiceInterface(serviceClass);
        if (webService == null) {
            throw new IllegalArgumentException(service + " is not web service");
        }
        RequestHandler handler = new JavawsDelegateHandler(service, config);
        if (serviceConfig == null) {
            String endpoint = getEndpoint(serviceClass, webService);
            serviceConfig = new ServiceConfigDesc(Protocol.HTTP,
                    com.plexobject.service.Method.POST, Void.class,
                    config.getDefaultCodecType(), DEFAULT_VERSION, endpoint,
                    true, new String[0], 1);
        }
        //
        int countExported = 0;
        for (final Method implMethod : serviceClass.getMethods()) {
            Method iMethod = getExported(webService, implMethod);
            if (iMethod != null) {
                ((JavawsDelegateHandler) handler)
                        .addMethod(iMethod, implMethod);
                countExported++;
            }
        }
        if (countExported > 0) {
            return Pair.of(serviceConfig, handler);
        } else {
            throw new IllegalArgumentException("No exported methods in "
                    + service);
        }
    }

    private static String getEndpoint(Class<?> serviceClass, Class<?> webService) {
        Path path = serviceClass.getAnnotation(Path.class);
        String endpoint = null;
        if (path == null) {
            endpoint = "/" + webService.getSimpleName();
        } else {
            endpoint = path.value();
        }
        return endpoint;
    }

    private static Method getExported(Class<?> webService, Method implMethod) {
        try {
            Method iMethod = webService.getMethod(implMethod.getName(),
                    implMethod.getParameterTypes());
            if (implMethod.getParameterTypes().length <= 1 && iMethod != null) {
                WebMethod webMethod = implMethod.getAnnotation(WebMethod.class);
                if (webMethod == null || !webMethod.exclude()) {
                    return iMethod;
                }
            }
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
        }

        return null;
    }

    static Class<?> getWebServiceInterface(Class<?> serviceClass) {
        Class<?>[] interfaces = serviceClass.getInterfaces();
        for (Class<?> iface : interfaces) {
            WebService webService = iface.getAnnotation(WebService.class);
            if (webService != null) {
                return iface;
            }
        }
        return null;
    }

}
