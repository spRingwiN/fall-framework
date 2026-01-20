package com.eric.fall.around;

import com.eric.fall.annotation.Around;
import com.eric.fall.annotation.Component;
import com.eric.fall.annotation.Value;

@Component
@Around("aroundInvocationHandler")
public class OriginBean {

    @Value("${customer.name}")
    public String name;

    @Polite
    public String hello() {
        return "Hello, " + name + ".";
    }

    public String morning() {
        return "Morning, " + name + ".";
    }

}
