package com.plexobject.handler.javaws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.RequestHandlerAdapter;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;

public class RequestHandlerAdapterJavaws implements RequestHandlerAdapter {
    private static final Logger logger = Logger
            .getLogger(RequestHandlerAdapterJavaws.class);

    private static final String DEFAULT_VERSION = "1.0";
    private static final String[] DEFAULT_ROLES = new String[0];
    private final ServiceRegistry registry;

    public RequestHandlerAdapterJavaws(final ServiceRegistry registry) {
        this.registry = registry;
    }

    public Map<ServiceConfigDesc, RequestHandler> createFromPackages(
            String... pkgNames) {
        Collection<Class<?>> serviceClasses = ReflectUtils.getAnnotatedClasses(
                WebService.class, pkgNames);
        Map<ServiceConfigDesc, RequestHandler> handlers = new HashMap<>();
        for (Class<?> serviceClass : serviceClasses) {
            if (!serviceClass.isInterface()) {
                Class<?> webService = ReflectUtils
                        .getWebServiceInterface(serviceClass);
                if (webService == null) {
                    continue;
                }
                try {
                    Map<ServiceConfigDesc, RequestHandler> handlerAndConfig = create(
                            serviceClass, (ServiceConfigDesc) null);
                    handlers.putAll(handlerAndConfig);
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }
        }
        return handlers;
    }

    @Override
    public Map<ServiceConfigDesc, RequestHandler> create(Class<?> serviceClass,
            ServiceConfigDesc serviceConfig) {
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
    public Map<ServiceConfigDesc, RequestHandler> create(Object service,
            String endpoint, RequestMethod method) {
        ServiceConfigDesc serviceConfig = endpoint != null ? buildConfig(
                endpoint, method) : null;
        return create(service, serviceConfig);
    }

    @Override
    public Map<ServiceConfigDesc, RequestHandler> create(Object service,
            ServiceConfigDesc defaultServiceConfig) {
        Class<?> serviceClass = service.getClass();
        Class<?> webService = ReflectUtils.getWebServiceInterface(serviceClass);
        if (webService == null) {
            throw new IllegalArgumentException(service + " is not web service");
        }
        Map<RequestMethod, ServiceConfigDesc> configs = new HashMap<>();
        Map<ServiceConfigDesc, RequestHandler> handlers = new HashMap<>();
        if (defaultServiceConfig == null) {
            String endpoint = getEndpoint(serviceClass, webService);
            defaultServiceConfig = buildConfig(endpoint, RequestMethod.POST);
        }
        //
        for (final Method implMethod : serviceClass.getMethods()) {
            Method iMethod = getExported(webService, implMethod);
            if (iMethod != null) {
                String itemName = getItemTag(iMethod, implMethod);
                String[] paramNames = getParams(implMethod);
                if (implMethod.getParameterTypes().length > 1
                        && paramNames.length != implMethod.getParameterTypes().length) {
                    continue; // skip methods that take more than one parameter
                              // and does not defined form or query parameters
                }
                RequestMethod requestMethod = getRequestMethod(iMethod,
                        implMethod);
                ServiceConfigDesc serviceConfig = configs.get(requestMethod);
                if (serviceConfig == null) {
                    serviceConfig = new ServiceConfigDesc(defaultServiceConfig,
                            requestMethod);
                    configs.put(requestMethod, serviceConfig);
                }
                RequestHandler handler = handlers.get(serviceConfig);
                if (handler == null) {
                    handler = new JavawsDelegateHandler(service, registry);
                    handlers.put(serviceConfig, handler);
                }
                ((JavawsDelegateHandler) handler)
                        .addMethod(new JavawsDelegateHandler.MethodInfo(
                                iMethod, implMethod, itemName, requestMethod,
                                paramNames));
                if (logger.isDebugEnabled()) {
                    logger.debug("Added handler " + handler + " for "
                            + requestMethod + " " + iMethod.getName()
                            + ", service " + service.getClass().getSimpleName());
                }
            }
        }
        return handlers;
    }

    private String getItemTag(final Method iMethod, final Method implMethod) {
        WebParam webParam = ReflectUtils.getWebParamFor(iMethod);
        if (webParam == null) {
            webParam = ReflectUtils.getWebParamFor(implMethod);
        }
        String responseItemTag = webParam != null ? webParam.name() : registry
                .getConfiguration().getProperty("javaWs.defaultItem");
        return responseItemTag;
    }

    private String[] getParams(final Method implMethod) {
        List<String> params = new ArrayList<>();
        for (Annotation[] annotations : implMethod.getParameterAnnotations()) {
            for (Annotation a : annotations) {
                if (a instanceof QueryParam) {
                    params.add(((QueryParam) a).value());
                } else if (a instanceof FormParam) {
                    params.add(((FormParam) a).value());
                }
            }
        }
        return params.toArray(new String[params.size()]);
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
            if (iMethod != null) {
                WebMethod iWebMethod = iMethod.getAnnotation(WebMethod.class);
                WebMethod implWebMethod = implMethod
                        .getAnnotation(WebMethod.class);
                if ((iWebMethod == null || !iWebMethod.exclude())
                        && (implWebMethod == null || !implWebMethod.exclude())) {
                    return iMethod;
                }
            }
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
        }

        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static RequestMethod getRequestMethod(Method iMethod,
            Method implMethod) {
        try {
            Class[] classes = { GET.class, POST.class, PUT.class, DELETE.class,
                    HEAD.class };
            RequestMethod[] methods = { RequestMethod.GET, RequestMethod.POST,
                    RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.HEAD };
            for (int i = 0; i < classes.length; i++) {
                if (iMethod.getAnnotation(classes[i]) != null
                        || implMethod.getAnnotation(classes[i]) != null) {
                    return methods[i];
                }
            }
        } catch (Exception e) {
        }
        // default is POST
        return RequestMethod.POST;
    }

    private ServiceConfigDesc buildConfig(String endpoint, RequestMethod method) {
        return new ServiceConfigDesc(Protocol.HTTP, method, Void.class,
                registry.getConfiguration().getDefaultCodecType(),
                DEFAULT_VERSION, endpoint, true, DEFAULT_ROLES, 1);
    }

}
