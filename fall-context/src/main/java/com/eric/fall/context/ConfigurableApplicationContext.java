package com.eric.fall.context;

import jakarta.annotation.Nullable;

import java.util.List;

public interface ConfigurableApplicationContext extends ApplicationContext {

    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> RequiredType);

    Object createBeanAsEarlySingleton(BeanDefinition def);
















}
