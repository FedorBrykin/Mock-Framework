package ru.nsu.api;

import ru.nsu.core.factory.MockFactory;
import ru.nsu.core.handler.MockHandler;
import ru.nsu.core.registy.StubbingRegistry;
import ru.nsu.annotation.Mock;
import ru.nsu.core.progress.MockingProgress;

import java.lang.reflect.Field;

public class JokeMock {

    private static final StubbingRegistry registry = StubbingRegistry.getInstance();
    private static final MockingProgress progress = MockingProgress.getInstance();

    public static <T> T mock(Class<T> classToMock) {
        return MockFactory.createMock(classToMock);
    }

    public static void resetMocks() {
        registry.reset();
    }

    public static <T> OngoingStubbing<T> when(T invocation) {
        return new OngoingStubbing<>();
    }

    public static void initMocks(Object testInstance) {
        Class<?> clazz = testInstance.getClass();

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Mock.class)) {
                    createMockForField(testInstance, field);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static void createMockForField(Object testInstance, Field field) {
        try {
            field.setAccessible(true);

            Class<?> fieldType = field.getType();
            Object mock = MockFactory.createMock(fieldType);
            field.set(testInstance, mock);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock for field: " + field.getName(), e);
        }
    }
}