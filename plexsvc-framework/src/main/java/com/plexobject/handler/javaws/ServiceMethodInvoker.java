package com.plexobject.handler.javaws;

import java.lang.reflect.Method;

/**
 * This interface allows customization of how methods are invoked via reflection
 * 
 * @author shahzad bhatti
 *
 */
public interface ServiceMethodInvoker {
    Object invoke(Object object, Method method, Object... args)
            throws Exception;
}
