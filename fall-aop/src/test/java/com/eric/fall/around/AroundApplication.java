package com.eric.fall.around;

import com.eric.fall.annotation.Bean;
import com.eric.fall.annotation.Component;
import com.eric.fall.annotation.Configuration;
import com.eric.fall.aop.AroundProxyBeanPostProcessor;

@Configuration
public class AroundApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }

}
