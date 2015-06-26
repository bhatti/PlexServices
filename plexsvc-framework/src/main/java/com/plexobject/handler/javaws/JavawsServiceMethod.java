package com.plexobject.handler.javaws;

import java.lang.reflect.Method;
import java.util.Map;

import com.plexobject.service.RequestMethod;

class JavawsServiceMethod {
    final Method iMethod;
    final Method implMethod;
    final RequestMethod requestMethod;
    final String[] paramNames;
    final String methodPath;

    JavawsServiceMethod(Method iMethod, Method implMethod,
            RequestMethod requestMethod, String[] paramNames, String methodPath) {
        this.iMethod = iMethod;
        this.implMethod = implMethod;
        this.requestMethod = requestMethod;
        this.paramNames = paramNames;
        this.methodPath = methodPath;
    }

    boolean useNameParams() {
        return paramNames.length > 0
                && paramNames.length == iMethod.getParameterTypes().length;
    }

    boolean useMapProperties() {
        return iMethod.getParameterTypes().length == 1
                && Map.class == iMethod.getParameterTypes()[0];
    }
}
