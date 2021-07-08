package com.mzh.emock.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EMock {
    int order() default Integer.MIN_VALUE;
    String name() default "";
    boolean objectEnableMock() default true;
    boolean methodEnableMock() default true;
    String[] reverseEnabledMethod() default {};
}
