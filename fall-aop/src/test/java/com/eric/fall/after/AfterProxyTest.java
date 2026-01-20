package com.eric.fall.after;

import com.eric.fall.context.AnnotationConfigApplication;
import com.eric.fall.io.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AfterProxyTest {

    @Test
    public void testAfterProxy() {
        try (var ctx = new AnnotationConfigApplication(AfterApplication.class, createPropertyResolver())) {
            GreetingBean proxy = ctx.getBean(GreetingBean.class);
            // should change return value:
            assertEquals("Hello, World!", proxy.hello("World"));
            assertEquals("Morning, Alice!", proxy.morning("Alice"));
        }
    }

    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        var pr = new PropertyResolver( ps);
        return pr;
    }

}
