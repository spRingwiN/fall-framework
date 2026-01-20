package com.eric.fall.context;

public interface BeanPostProcessor {

    /**
     * Invoked after new Bean()
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Invoked after bean.init called
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Invoked before bean.setXyz() called
     * @param bean
     * @param beanName
     * @return
     */
    default Object postProcessOnSetProperty (Object bean, String beanName) {
        return bean;
    }

}
