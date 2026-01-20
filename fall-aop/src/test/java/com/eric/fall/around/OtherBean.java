package com.eric.fall.around;

import com.eric.fall.annotation.Autowired;
import com.eric.fall.annotation.Component;
import com.eric.fall.annotation.Order;

@Component
@Order(0)
public class OtherBean {

    public OriginBean origin;

    public OtherBean(@Autowired OriginBean origin) {
        this.origin = origin;
    }

}
