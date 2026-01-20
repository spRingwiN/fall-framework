package com.eric.fall.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {

    /**
     * Bean name. Default to simple class name with first letter to lowercase
     * @return
     */
    String value() default "";

}
