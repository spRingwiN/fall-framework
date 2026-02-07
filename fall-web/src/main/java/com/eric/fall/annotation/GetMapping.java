package com.eric.fall.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GetMapping {

    /**
     * URL mapping
     */
    String value() default "";

}
