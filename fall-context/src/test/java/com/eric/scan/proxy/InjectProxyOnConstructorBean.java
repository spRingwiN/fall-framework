package com.eric.scan.proxy;


import com.eric.fall.annotation.Autowired;
import com.eric.fall.annotation.Component;

@Component
public class InjectProxyOnConstructorBean {

    public final OriginBean injected;

    public InjectProxyOnConstructorBean(@Autowired OriginBean injected) {
        this.injected = injected;
    }

}
