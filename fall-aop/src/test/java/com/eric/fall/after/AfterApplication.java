package com.eric.fall.after;

import com.eric.fall.annotation.Bean;
import com.eric.fall.annotation.ComponentScan;
import com.eric.fall.annotation.Configuration;
import com.eric.fall.aop.AroundProxyBeanPostProcessor;

@Configuration
@ComponentScan
public class AfterApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor()
    {
        return new AroundProxyBeanPostProcessor();
    }














}
