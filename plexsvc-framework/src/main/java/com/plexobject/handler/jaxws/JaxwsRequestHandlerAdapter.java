package com.plexobject.handler.jaxws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;

import com.plexobject.domain.Pair;
import com.plexobject.handler.RequestHandler;
import com.plexobject.handler.RequestHandlerAdapter;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfigDesc;
import com.plexobject.service.ServiceRegistry;
import com.plexobject.util.ReflectUtils;

public class JaxwsRequestHandlerAdapter implements RequestHandlerAdapter {
    private static final Logger logger = Logger
            .getLogger(JaxwsRequestHandlerAdapter.class);
    private static final String DEFAULT_VERSION = "1.0";
    private static final String[] DEFAULT_ROLES = new String[0];
    private final ServiceRegistry registry;

    public JaxwsRequestHandlerAdapter(final ServiceRegistry registry) {
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
            throw new IllegalArgumentException(service
                    + " does not define WebService annotations");
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
                String methodPath = getMethodPath(iMethod, implMethod);
                if (methodPath == null) {
                    methodPath = defaultServiceConfig.endpoint();
                }
                Pair<String, String>[] paramNamesAndDefaults = getParamsAndDefaults(implMethod);
                if (implMethod.getParameterTypes().length > 1
                        && paramNamesAndDefaults.length != implMethod
                                .getParameterTypes().length) {
                    continue; // skip methods that take more than one parameter
                              // and does not defined form or query parameters
                }
                RequestMethod requestMethod = getRequestMethod(iMethod,
                        implMethod);
                ServiceConfigDesc serviceConfig = ServiceConfigDesc
                        .builder(defaultServiceConfig).setMethod(requestMethod)
                        .setEndpoint(methodPath).build();
                RequestHandler handler = handlers.get(serviceConfig);
                if (handler == null) {
                    handler = new JaxwsDelegateHandler(service, registry);
                    handlers.put(serviceConfig, handler);
                }
                //
                ((JaxwsDelegateHandler) handler)
                        .addMethod(new JaxwsServiceMethod(iMethod, implMethod,
                                requestMethod, paramNamesAndDefaults,
                                methodPath));
                if (logger.isDebugEnabled()) {
                    logger.debug("Added handler "
                            + handler.getClass().getSimpleName() + " for "
                            + requestMethod + " " + iMethod.getName()
                            + ", path " + methodPath + ", service "
                            + service.getClass().getSimpleName());
                }
            }
        }
        return handlers;
    }

    private String getMethodPath(final Method iMethod, final Method implMethod) {
        Path path = iMethod.getAnnotation(Path.class);
        if (path == null) {
            path = implMethod.getAnnotation(Path.class);
        }
        return path == null ? null : path.value();
    }

    @SuppressWarnings("unchecked")
    private Pair<String, String>[] getParamsAndDefaults(final Method implMethod) {
        List<Pair<String, String>> paramsAndDefaults = new ArrayList<>();
        for (Annotation[] annotations : implMethod.getParameterAnnotations()) {
            String param = null;
            String defValue = null;
            for (Annotation a : annotations) {
                if (a instanceof QueryParam) {
                    param = ((QueryParam) a).value();
                } else if (a instanceof FormParam) {
                    param = ((FormParam) a).value();
                } else if (a instanceof PathParam) {
                    param = ((PathParam) a).value();
                } else if (a instanceof HeaderParam) {
                    param = ((HeaderParam) a).value();
                } else if (a instanceof CookieParam) {
                    param = ((CookieParam) a).value();
                } else if (a instanceof FormParam) {
                    param = ((FormParam) a).value();
                } else if (a instanceof DefaultValue) {
                    defValue = ((DefaultValue) a).value();
                }
            }
            if (param != null) {
                paramsAndDefaults.add(Pair.of(param, defValue));
            }
        }
        return paramsAndDefaults.toArray(new Pair[paramsAndDefaults.size()]);
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
