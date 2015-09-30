package com.plexobject.handler.ws;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.plexobject.handler.Request;
import com.plexobject.service.RequestMethod;
import com.plexobject.util.ReflectUtils.Param;
import com.plexobject.util.ReflectUtils.ParamType;

class WSServiceMethod {
    final Method iMethod;
    final Method implMethod;
    final RequestMethod requestMethod;
    final Param[] params;
    final String methodPath;
    final boolean hasMultipleParamTypes;

    static class Builder {
        Method iMethod;
        Method implMethod;
        RequestMethod requestMethod;
        List<Param> params = new ArrayList<>();
        String methodPath;
        boolean hasMultipleParamTypes;

        WSServiceMethod build() {
            return new WSServiceMethod(iMethod, implMethod, requestMethod,
                    params.toArray(new Param[params.size()]), methodPath,
                    hasMultipleParamTypes);
        }

        boolean canBuild() {
            int annotationParameterIndex = 0;
            Class<?>[] methodParamTypes = implMethod.getParameterTypes();
            for (Annotation[] annotations : implMethod
                    .getParameterAnnotations()) {
                String paramName = null;
                String defValue = null;
                int numObjectParams = 0;
                //
                for (Annotation a : annotations) {
                    if (a instanceof QueryParam) {
                        paramName = ((QueryParam) a).value();
                    } else if (a instanceof FormParam) {
                        paramName = ((FormParam) a).value();
                    } else if (a instanceof PathParam) {
                        paramName = ((PathParam) a).value();
                    } else if (a instanceof HeaderParam) {
                        paramName = ((HeaderParam) a).value();
                    } else if (a instanceof CookieParam) {
                        paramName = ((CookieParam) a).value();
                    } else if (a instanceof FormParam) {
                        paramName = ((FormParam) a).value();
                    } else if (a instanceof DefaultValue) {
                        defValue = ((DefaultValue) a).value();
                    }
                }
                if (paramName != null) {
                    params.add(new Param(ParamType.QUERY_FORM_PARAM, paramName,
                            defValue));
                } else if (methodParamTypes[annotationParameterIndex] == Request.class) {
                    params.add(new Param(ParamType.REQUEST_PARAM, null, null));
                    hasMultipleParamTypes = true;
                } else if (methodParamTypes[annotationParameterIndex] == Map.class) {
                    params.add(new Param(ParamType.MAP_PARAM, null, null));
                    hasMultipleParamTypes = true;
                } else if (numObjectParams == 0
                        && (requestMethod == RequestMethod.POST || requestMethod == RequestMethod.PUT)) {
                    params.add(new Param(ParamType.JSON_PARAM, null, null));
                    numObjectParams++;
                    hasMultipleParamTypes = true;
                } else {
                    return false;
                }
                annotationParameterIndex++;
            }
            return true;
        }
    }

    WSServiceMethod(Method iMethod, Method implMethod,
            RequestMethod requestMethod, Param[] params, String methodPath,
            boolean hasMultipleParamTypes) {
        this.iMethod = iMethod;
        this.implMethod = implMethod;
        this.requestMethod = requestMethod;
        this.params = params;
        this.methodPath = methodPath;
        this.hasMultipleParamTypes = hasMultipleParamTypes;
    }

    @Override
    public String toString() {
        return "JavawsServiceMethod [iMethod=" + iMethod.getName()
                + ", requestMethod=" + requestMethod
                + ", paramNamesAndDefaults=" + Arrays.toString(params)
                + ", methodPath=" + methodPath + "]";
    }

}
