package com.plexobject.handler.jaxws;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import com.plexobject.domain.Pair;
import com.plexobject.service.RequestMethod;

class JaxwsServiceMethod {
    final Method iMethod;
    final Method implMethod;
    final RequestMethod requestMethod;
    final Pair<String, String>[] paramNamesAndDefaults;
    final String methodPath;

    JaxwsServiceMethod(Method iMethod, Method implMethod,
            RequestMethod requestMethod,
            Pair<String, String>[] paramNamesAndDefaults, String methodPath) {
        this.iMethod = iMethod;
        this.implMethod = implMethod;
        this.requestMethod = requestMethod;
        this.paramNamesAndDefaults = paramNamesAndDefaults;
        this.methodPath = methodPath;
    }

    boolean useNameParams() {
        return paramNamesAndDefaults.length > 0
                && paramNamesAndDefaults.length == iMethod.getParameterTypes().length;
    }

    boolean useMapProperties(String payload) {
        return (payload == null || payload.length() == 0)
                && iMethod.getParameterTypes().length == 1
                && Map.class == iMethod.getParameterTypes()[0];
    }

    @Override
    public String toString() {
        return "JavawsServiceMethod [iMethod=" + iMethod.getName()
                + ", requestMethod=" + requestMethod
                + ", paramNamesAndDefaults="
                + Arrays.toString(paramNamesAndDefaults) + ", methodPath="
                + methodPath + "]";
    }

}
