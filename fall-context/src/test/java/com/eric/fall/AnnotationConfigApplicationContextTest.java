package com.eric.fall;

import com.eric.fall.context.AnnotationConfigApplication;
import com.eric.fall.io.PropertyResolver;
import com.eric.scan.ScanApplication;
import com.eric.scan.proxy.InjectProxyOnConstructorBean;
import com.eric.scan.proxy.InjectProxyOnPropertyBean;
import com.eric.scan.proxy.OriginBean;
import com.eric.scan.proxy.SecondProxyBean;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationConfigApplicationContextTest {

    @Test
    public void testProxy() {
        var ctx = new AnnotationConfigApplication(ScanApplication.class, createPropertyResolver());

        // test proxy
        OriginBean proxy = ctx.getBean(OriginBean.class);
        assertSame(SecondProxyBean.class, proxy.getClass());
        assertEquals("Scan App", proxy.getName());
        assertEquals("v1.0", proxy.getVersion());
        // make sure proxy field is not injected
        assertNull(proxy.name);
        assertNull(proxy.version);

        // other beans are injected proxy instance
        var inject1 = ctx.getBean(InjectProxyOnConstructorBean.class);
        var inject2 = ctx.getBean(InjectProxyOnPropertyBean.class);
        assertSame(proxy, inject1.injected);
        assertSame(proxy, inject2.injected);

    }


    PropertyResolver createPropertyResolver() {
        var ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("jdbc.url", "jdbc:hsqldb:file:testdb.tmp");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        ps.put("convert.boolean", "true");
        ps.put("convert.byte", "123");
        ps.put("convert.short", "12345");
        ps.put("convert.int", "1234567");
        ps.put("convert.long", "123456789000");
        ps.put("convert.float", "12345.6789");
        ps.put("convert.double", "123456789.87654321");
        ps.put("convert.localdate", "2025-12-06");
        ps.put("convert.localtime", "11:25:30");
        ps.put("convert.localdatetime", "2025-12-06T11:25:30");
        ps.put("convert.zoneddatetime", "2025-12-06T11:25:30+08:00[Asia/Shanghai]");
        ps.put("convert.duration", "P2DT3H4M");
        ps.put("convert.zoneid", "Asia/Shanghai");
        var pr = new PropertyResolver(ps);
        return pr;



    }

}
