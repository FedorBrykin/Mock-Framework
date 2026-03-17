package ru.nsu.core.factory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import ru.nsu.core.handler.MockHandler;

public class MockFactory {

    private static final ByteBuddy byteBuddy = new ByteBuddy();

    @SuppressWarnings("unchecked")
    public static <T> T createMock(Class<T> classToMock) {
        try {
            if (classToMock.isInterface()) {
                return createInterfaceMock(classToMock);
            } else {
                return createClassMock(classToMock);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock for " + classToMock.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInterfaceMock(Class<T> interfaceToMock) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                interfaceToMock.getClassLoader(),
                new Class<?>[]{interfaceToMock},
                new MockHandler()
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T createClassMock(Class<T> classToMock) throws Exception {
        return (T) byteBuddy.subclass(classToMock)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(MockHandler.class))
                .make()
                .load(classToMock.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
    }
}