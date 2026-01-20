package com.eric.fall.around;

import com.eric.fall.annotation.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Component
public class AroundInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //
        Object result = method.invoke(proxy, args);
        if (method.getAnnotation(Polite.class) != null) {

            if (result instanceof String s) {
                if (s.endsWith( ".")) {
                    return s.substring(0, s.length() - 1) + "!";
                }
            }

        }
        return result;
    }
}
