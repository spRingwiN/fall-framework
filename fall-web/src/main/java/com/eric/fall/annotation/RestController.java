package com.eric.fall.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RestController {

    /**
     * Bean name. Default to simple class name with first letter lowercase
     * @return
     */
    String value() default "";

}
