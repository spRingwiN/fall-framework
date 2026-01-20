package com.eric.scan.proxy;

import com.eric.fall.annotation.Component;
import com.eric.fall.annotation.Value;

@Component
public class OriginBean {

    @Value("${app.title}")
    public String name;

    public String version;

    @Value("${app.version}")
    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }
    public String getVersion() {
        return version;
    }




}
