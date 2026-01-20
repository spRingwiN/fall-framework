package com.eric.fall.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    /**
     * Is required
     * @return
     */
    boolean value() default true;

    /**
     * Bean name if set
     * @return
     */
    String name() default "";

}
