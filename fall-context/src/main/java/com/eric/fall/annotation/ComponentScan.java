package com.eric.fall.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ComponentScan {

    /**
     * Package names to scan. Default to current package
     * @return
     */
    String[] value() default {};

}
