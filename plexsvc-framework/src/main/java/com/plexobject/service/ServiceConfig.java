package com.plexobject.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.plexobject.encode.CodecType;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServiceConfig {
    Class<?> contentsClass() default Void.class;

    Protocol protocol();

    CodecType codec() default CodecType.JSON;

    RequestMethod method();

    String version() default "1.0";

    String endpoint() default "";

    boolean recordStatsdMetrics() default true;

    String[] rolesAllowed() default {};

    int concurrency() default 1;
}
