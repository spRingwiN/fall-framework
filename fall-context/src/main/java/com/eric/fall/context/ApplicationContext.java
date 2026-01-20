package com.eric.fall.context;

import java.io.Closeable;
import java.util.List;

public interface ApplicationContext extends Closeable {
    /**
     * 是否存在指定name的Bean
     * @param name
     * @return
     */
    boolean containsBean(String name);

    /**
     * 根据name返回唯一Bean， 未找到抛出NoSuchBeanDefinitionException
     * @param name
     * @return
     * @param <T>
     */
    <T> T getBean(String name);

    /**
     * 根据name返回唯一的Bean，未找到抛出NoSuchBeanDefinitionException，找到
     * 但type不符抛出BeanNotOfRequiredTypeException
     * @param name
     * @param requiredType
     * @return
     * @param <T>
     */
    <T> T getBean(String name, Class<T> requiredType);

    /**
     * 根据type返回唯一的Bean，未找到抛出NoSuchBeanDefinitionException
     * @param requiredType
     * @return
     * @param <T>
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * 根据type返回一组Bean，未找到返回空List
     * @return
     * @param <T>
     */
    <T> List<T> getBeans(Class<T> requiredType);

    /**
     * 关闭并执行所有bean的destroy方法
     */
    void close();

















}
