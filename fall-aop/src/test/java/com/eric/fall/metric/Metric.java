package com.eric.fall.metric;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Metric {

    String value() default "";

}
