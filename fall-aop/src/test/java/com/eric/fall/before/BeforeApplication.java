package com.eric.fall.before;

import com.eric.fall.annotation.Bean;
import com.eric.fall.annotation.Configuration;
import com.eric.fall.aop.AroundProxyBeanPostProcessor;

@Configuration
public class BeforeApplication {

    @Bean
    AroundProxyBeanPostProcessor createAroundProxyBeanPostProcessor() {
        return new AroundProxyBeanPostProcessor();
    }

}
