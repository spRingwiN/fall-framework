package com.eric.scan.proxy;

import com.eric.fall.annotation.Autowired;
import com.eric.fall.annotation.Component;

@Component
public class InjectProxyOnPropertyBean {

    @Autowired
    public OriginBean injected;

}
