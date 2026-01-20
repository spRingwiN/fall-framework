package com.eric.fall.around;

import com.eric.fall.context.AnnotationConfigApplication;
import com.eric.fall.io.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class AroundProxyTest {

    @Test
    public void testAroundProxy() {
        try (var ctx = new AnnotationConfigApplication(AroundApplication.class, createPropertyResolver())) {
            OriginBean proxy = ctx.getBean(OriginBean.class);
            // OriginBean$ByteBuddy$8nOd1Fcq
            System.out.println(proxy.getClass().getName());

            // proxy class, not origin class
            assertNotSame(OriginBean.class, proxy.getClass());
            // proxy.name not injected
            assertNull(proxy.name);

            assertEquals("Hello, Bob!", proxy.hello());
            assertEquals("Morning, Bob.", proxy.morning());

            // test injected proxy:
            OtherBean other = ctx.getBean(OtherBean.class);
            assertSame(proxy, other.origin);
            assertEquals("Hello, Bob!", other.origin.hello());

        }
    }


    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("customer.name", "Bob");
        return new PropertyResolver(ps);
    }

}
