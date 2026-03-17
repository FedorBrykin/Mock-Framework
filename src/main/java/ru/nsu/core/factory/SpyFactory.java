package ru.nsu.core.factory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import ru.nsu.core.handler.SpyHandler;

import java.lang.reflect.Proxy;

public class SpyFactory {

    private static final ByteBuddy byteBuddy = new ByteBuddy();

    @SuppressWarnings("unchecked")
    public static <T> T createSpy(T objectToSpy) {
        if (objectToSpy == null) {
            throw new IllegalArgumentException("Cannot spy on null object");
        }

        Class<?> targetClass = objectToSpy.getClass();

        try {
            if (targetClass.isInterface() || hasInterface(targetClass)) {
                return createInterfaceSpy(objectToSpy);
            } else {
                return createClassSpy(objectToSpy);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create spy for " + targetClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInterfaceSpy(T objectToSpy) {
        Class<?> targetClass = objectToSpy.getClass();
        Class<?>[] interfaces = targetClass.getInterfaces();

        if (interfaces.length == 0) {
            return (T) Proxy.newProxyInstance(
                    targetClass.getClassLoader(),
                    new Class<?>[]{targetClass},
                    new SpyHandler(objectToSpy)
            );
        }

        return (T) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                interfaces,
                new SpyHandler(objectToSpy)
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T createClassSpy(T objectToSpy) throws Exception {
        Class<?> targetClass = objectToSpy.getClass();

        SpyHandler handler = new SpyHandler(objectToSpy);

        return (T) byteBuddy.subclass(targetClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(handler))  // Делегируем экземпляру, а не классу
                .make()
                .load(targetClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
    }

    private static boolean hasInterface(Class<?> clazz) {
        return clazz.getInterfaces().length > 0;
    }
}