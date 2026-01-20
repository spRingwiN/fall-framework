package com.eric.fall.before;

import com.eric.fall.context.AnnotationConfigApplication;
import com.eric.fall.io.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BeforeProxyTest {

    @Test
    public void testBeforeProxy() {
        try (var ctx = new AnnotationConfigApplication(BeforeApplication.class, createPropertyResolver())) {
            BusinessBean proxy = ctx.getBean(BusinessBean.class);
            // should pring log:
            assertEquals("Hello, Bob.", proxy.hello("Bob"));
            assertEquals("Morning, Alice.", proxy.morning("Alice"));
        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        return new PropertyResolver(ps);
    }
}
