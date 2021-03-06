package com.plexobject.handler.ws;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.log4j.Logger;

import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.RequestHandlerAdapter;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;

public class WSRequestHandlerAdapter implements RequestHandlerAdapter {
    private static final Logger logger = Logger.getLogger(WSRequestHandlerAdapter.class);
    private static final String DEFAULT_VERSION = "1.0";
    private static final String[] DEFAULT_ROLES = new String[0];
    private final ServiceRegistry registry;

    public WSRequestHandlerAdapter(final ServiceRegistry registry) {
        this.registry = registry;
    }

    public Map<ServiceConfigDesc, RequestHandler> createFromPackages(String... pkgNames) {
        Collection<Class<?>> serviceClasses = ReflectUtils.getAnnotatedClasses(WebService.class,
                pkgNames);
        Map<ServiceConfigDesc, RequestHandler> handlers = new HashMap<>();
        for (Class<?> serviceClass : serviceClasses) {
            if (!serviceClass.isInterface()) {
                Class<?> webService = ReflectUtils.getWebServiceInterface(serviceClass);
                if (webService == null) {
                    continue;
                }
                try {
                    Map<ServiceConfigDesc, RequestHandler> handlerAndConfig = create(serviceClass,
                            (ServiceConfigDesc) null);
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
    public Map<ServiceConfigDesc, RequestHandler> create(Object service, String endpoint,
            RequestMethod method) {
        ServiceConfigDesc serviceConfig = endpoint != null ? buildConfig(endpoint, method) : null;
        return create(service, serviceConfig);
    }

    @Override
    public Map<ServiceConfigDesc, RequestHandler> create(Object service,
            ServiceConfigDesc defaultServiceConfig) {
        Class<?> serviceClass = service.getClass();
        Class<?> webService = ReflectUtils.getWebServiceInterface(serviceClass);
        if (webService == null) {
            throw new IllegalArgumentException(service + " does not define WebService annotations");
        }
        Map<ServiceConfigDesc, RequestHandler> handlers = new HashMap<>();
        if (defaultServiceConfig == null) {
            String endpoint = getEndpoint(serviceClass, webService);
            defaultServiceConfig = buildConfig(endpoint, RequestMethod.POST);
        }
        //
        for (final Method implMethod : serviceClass.getMethods()) {
            Method iMethod = getExported(webService, implMethod);
            if (iMethod != null) {
                WSServiceMethod.Builder methodBuilder = new WSServiceMethod.Builder();
                methodBuilder.iMethod = iMethod;
                methodBuilder.implMethod = implMethod;
                methodBuilder.methodPath = getMethodPath(defaultServiceConfig.endpoint(), iMethod,
                        implMethod);
                if (methodBuilder.methodPath == null) {
                    methodBuilder.methodPath = defaultServiceConfig.endpoint();
                }
                //
                methodBuilder.requestMethod = getRequestMethod(iMethod, implMethod);
                // we now allow users to send query/form parameters, JSON object
                // (if POST) and Request parameter
                if (!methodBuilder.canBuild()) {
                    logger.warn("PLEXSVC: Skipping handler for " + iMethod.getName()
                            + " method, path " + methodBuilder.methodPath + ", service "
                            + service.getClass().getSimpleName());

                    continue;
                }
                //
                ServiceConfigDesc serviceConfig = ServiceConfigDesc.builder(defaultServiceConfig)
                        .setMethod(methodBuilder.requestMethod)
                        .setEndpoint(methodBuilder.methodPath).build();
                RequestHandler handler = handlers.get(serviceConfig);
                if (handler == null) {
                    handler = new WSDelegateHandler(service, registry);
                    handlers.put(serviceConfig, handler);
                    //
                    ((WSDelegateHandler) handler).addMethod(methodBuilder.build());
                    logger.info("PLEXSVC: Added handler " + handler + " for "
                            + methodBuilder.requestMethod + " " + iMethod.getName() + ", path "
                            + methodBuilder.methodPath + ", service "
                            + service.getClass().getSimpleName());
                } else {
                    logger.error("PLEXSVC: Found duplicate handler " + handler + " for "
                            + methodBuilder.requestMethod + " " + iMethod.getName() + ", path "
                            + methodBuilder.methodPath + ", service "
                            + service.getClass().getSimpleName()
                            + ", overloaded method names are not supported!");
                }
            }
        }
        return handlers;
    }

    private String getMethodPath(String prefix, final Method iMethod, final Method implMethod) {
        Path path = iMethod.getAnnotation(Path.class);
        if (path == null) {
            path = implMethod.getAnnotation(Path.class);
        }
        return path == null ? null : (prefix != null ? prefix : "") + path.value();
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
                WebMethod implWebMethod = implMethod.getAnnotation(WebMethod.class);
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
    private static RequestMethod getRequestMethod(Method iMethod, Method implMethod) {
        try {
            Class[] classes = { GET.class, POST.class, PUT.class, DELETE.class, HEAD.class };
            RequestMethod[] methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.HEAD };
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
                registry.getConfiguration().getDefaultCodecType(), DEFAULT_VERSION, endpoint, true,
                DEFAULT_ROLES, 1);
    }
}
