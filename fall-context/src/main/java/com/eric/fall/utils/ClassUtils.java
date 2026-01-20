package com.eric.fall.utils;


import com.eric.fall.annotation.Bean;
import com.eric.fall.annotation.Component;
import com.eric.fall.exception.BeanDefinitionException;
import jakarta.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassUtils {

    /**
     * 递归查找Annotation
     * 示例： Annotation A 可以直接标注在class定义：
     * <code>
     * @A
     * public class Hello {}
     * </code>
     *
     * 或者Annotation B 标注了A， Class标注了B：
     * <code>
     * @A
     * public @interface B {}
     *
     * @B
     * public class Hello {}
     * </code>
     *
     * @param target
     * @param annoClass
     * @return
     * @param <A>
     */
    public static <A extends Annotation> A findAnnotation(Class<?> target, Class<A> annoClass) {
        A a = target.getAnnotation(annoClass);
        for (Annotation anno : target.getAnnotations()) {
            Class<? extends Annotation> annoType = anno.annotationType();
            if (!annoType.getPackageName().equals("java.lang.annotation")) {
                A found = findAnnotation(annoType, annoClass);
                if (found != null) {
                    if (a != null) {
                        throw new BeanDefinitionException("Duplicate annotation @" + annoClass.getName() + " found in " + target.getSimpleName());
                    }
                    a = found;
                }
            }
        }
        return a;

    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <A extends Annotation> A getAnnotation(Annotation[] annos, Class<A> annoClass) {
        for (Annotation anno : annos) {
            if (annoClass.isInstance(anno)) {
                return (A) anno;
            }
        }
        return null;
    }

    /**
     * Get bean name by:
     *
     * <code>
     * @Bean
     *  Hello createHell() {}
     * </code>
     * @param method
     * @return
     */
    public static String getBeanName(Method method) {
        Bean bean = method.getAnnotation(Bean.class);
        String name = bean.value();
        if (name.isEmpty()) {
            name = method.getName();
        }
        return name;
    }

    /**
     * Get bean name by:
     * <code>
     * @Component
     * public class Hello {}
     * </code>
     * @param clazz
     * @return
     */
    public static String getBeanName(Class<?> clazz) {
        String name = "";
        // 查找 @Component
        Component component = clazz.getAnnotation(Component.class);
        if (component != null) {
            name = component.value();
        } else {
            // 未找到@Component, 继续在其他注解中查找@Component
            for (Annotation anno : clazz.getAnnotations()) {
                if (findAnnotation(anno.annotationType(), Component.class) != null) {
                    try {
                        name = (String) anno.annotationType().getMethod("value").invoke(anno);
                    } catch (ReflectiveOperationException e) {
                        throw new BeanDefinitionException("Cannot get annotation value", e);
                    }
                }
            }
        }
        if (name.isEmpty()) {
            name = clazz.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    /**
     * Get non-arg method by @PostConstruct or @PreDestroy
     * Not search in super class
     * <code>
     *     @PostConstruct
     *     void init() {}
     * </code>
     * @param clazz
     * @param annoClass
     * @return
     */
    @Nullable
    public static Method findAnnotationMethod(Class<?> clazz, Class<? extends  Annotation> annoClass) {
        // try get declared method:
        List<Method> ms = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annoClass))
                .map(m -> {
                    if (m.getParameterCount() != 0) {
                        throw new BeanDefinitionException(
                                String.format("Method '%s' with @%s must not have argument: %s",
                                        m.getName(), annoClass.getSimpleName(), clazz.getName())
                        );
                    }
                    return m;
                }).collect(Collectors.toList());
        if (ms.isEmpty()) {
            return null;
        }
        if (ms.size() == 1) {
            return ms.get(0);
        }
        throw new BeanDefinitionException(
                String.format("Multiple methods with @%s found in %s",
                        annoClass.getSimpleName(), clazz.getName())
        );

    }

    public static Method getNamedMethod(Class<?> clazz, String methodName) {
        try {
            return clazz.getDeclaredMethod(methodName);
        } catch (ReflectiveOperationException e) {
            throw new BeanDefinitionException(
                    String.format("Method '%s' not found in class: %s", methodName, clazz.getName())
            );
        }
    }





}
