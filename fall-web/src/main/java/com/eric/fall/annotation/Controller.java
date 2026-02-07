package com.eric.fall.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {

    /**
     * Bean name. Default to simple class name with first letter lowercased
     * @return
     */
    String value() default "";

}
