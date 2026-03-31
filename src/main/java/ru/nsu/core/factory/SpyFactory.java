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
    public static <T> T createSpy(T realObject) {
        if (realObject == null) {
            throw new IllegalArgumentException("Cannot spy on null object");
        }

        Class<?> realClass = realObject.getClass();

        Class<?>[] interfaces = realClass.getInterfaces();
        if (interfaces.length > 0) {
            return (T) Proxy.newProxyInstance(
                    realClass.getClassLoader(),
                    interfaces,
                    new SpyHandler(realObject)
            );
        }

        return (T) createClassSpy(realClass, realObject);
    }

    private static Object createClassSpy(Class<?> classToSpy, Object delegate) {
        try {
            SpyHandler handler = new SpyHandler(delegate);
            Class<?> spyClass = byteBuddy.subclass(classToSpy)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(handler))
                    .make()
                    .load(classToSpy.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();

            return spyClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create spy for " + classToSpy.getName(), e);
        }
    }
}