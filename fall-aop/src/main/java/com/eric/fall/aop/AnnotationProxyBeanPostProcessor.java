package com.eric.fall.aop;

import com.eric.fall.context.ApplicationContextUtils;
import com.eric.fall.context.BeanDefinition;
import com.eric.fall.context.BeanPostProcessor;
import com.eric.fall.context.ConfigurableApplicationContext;
import com.eric.fall.exception.AopConfigException;
import com.eric.fall.exception.BeansException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public abstract class AnnotationProxyBeanPostProcessor<B extends Annotation> implements BeanPostProcessor {

    Map<String, Object> originBeans = new HashMap<>();

    Class<B> annotationClass;

    public AnnotationProxyBeanPostProcessor() {
        this.annotationClass = getParameterizedType();
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // has class-level @Annotation ?
        B anno = beanClass.getAnnotation(annotationClass);
        if (anno != null) {
            String handlerName;
            try {
                handlerName = (String) anno.annotationType().getMethod("value").invoke(anno);
            } catch (ReflectiveOperationException e) {
                throw new AopConfigException(String.format("@%s must have value() returned String type.", this.annotationClass.getSimpleName()), e);
            }
            Object proxy = createProxy(beanClass, bean, handlerName);
            originBeans.put(beanName, bean);
            return proxy;
        } else {
            return bean;
        }
    }

    @Override
    public Object postProcessOnSetProperty(Object bean, String beanName) {
        Object origin = this.originBeans.get(beanName);
        return origin != null ? origin : bean;
    }

    Object createProxy(Class<?> beanClass, Object bean, String handlerName) {
        ConfigurableApplicationContext ctx = (ConfigurableApplicationContext) ApplicationContextUtils.getRequiredApplicationContext();
        BeanDefinition def = ctx.findBeanDefinition(handlerName);
        if (def == null) {
            throw new AopConfigException(String.format("@%s proxy handler '%s' not found", this.annotationClass.getSimpleName(), handlerName));
        }
        Object handlerBean = def.getInstance();
        if (handlerBean == null) {
            handlerBean = ctx.createBeanAsEarlySingleton(def);
        }
        if (handlerBean instanceof InvocationHandler handler) {
            return ProxyResolver.getInstance().createProxy(bean, handler);
        } else {
            throw new AopConfigException(String.format("@%s proxy handler '%s' is not type of %s.",
                    this.annotationClass.getSimpleName(), handlerName, InvocationHandler.class.getName()));
        }
    }

    @SuppressWarnings("unchecked")
    private Class<B> getParameterizedType() {
        Type type = getClass().getGenericSuperclass();
        if (!(type instanceof ParameterizedType pt)) {
            throw new IllegalArgumentException(String.format("Class %s does not have parameterized type.", getClass().getName()));

        }
        Type[] types = pt.getActualTypeArguments();
        if (types.length != 1) {
            throw new IllegalArgumentException(String.format("Class %s does not have exactly one parameterized type.", getClass().getName()));
        }
        Type r = types[0];
        if (!(r instanceof Class<?>)) {
            throw new IllegalArgumentException(String.format("Class %s does not have parameterized type of class.", getClass().getName()));
        }

        return (Class<B>) r;
    }


}
