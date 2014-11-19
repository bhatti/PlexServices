package com.plexobject.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Field {
    String name();

    String regex() default "";

    int minLength() default 0;

    int maxLength() default Integer.MAX_VALUE;
}
