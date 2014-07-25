package com.plexobject.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ServiceConfig {
    String name() default "";

    String version() default "1.0";

    String path() default "";

    String method() default "";

    String contentType() default "";
}