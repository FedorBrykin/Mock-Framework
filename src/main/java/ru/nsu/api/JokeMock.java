package ru.nsu.api;

import ru.nsu.core.factory.MockFactory;
import ru.nsu.core.factory.SpyFactory;
import ru.nsu.core.handler.SpyHandler;
import ru.nsu.core.registy.StubbingRegistry;
import ru.nsu.annotation.Mock;
import ru.nsu.annotation.Spy;
import ru.nsu.core.progress.MockingProgress;
import ru.nsu.core.answer.Answer;
import ru.nsu.core.staticmock.StaticMockContext;
import ru.nsu.core.staticmock.StaticMockRegistry;

import java.lang.reflect.Field;

public class JokeMock {

    private static final StubbingRegistry registry = StubbingRegistry.getInstance();
    private static final MockingProgress progress = MockingProgress.getInstance();

    public static <T> T mock(Class<T> classToMock) {
        return MockFactory.createMock(classToMock);
    }

    public static <T> T spy(T object) {
        return SpyFactory.createSpy(object);
    }

    public static Stubber doReturn(Object value) {
        return new Stubber(Answer.returns(value));
    }

    public static Stubber doThrow(Throwable throwable) {
        return new Stubber(Answer.throwsException(throwable));
    }

    public static void reset() {
        registry.reset();
        SpyHandler.clearPendingAnswer();
        StaticMockRegistry.clear();
        progress.clear();
    }

    public static <T> OngoingStubbing<T> when(T invocation) {
        return new OngoingStubbing<>();
    }

    public static <T> MockedStatic<T> mockStatic(Class<T> classToMock) {
        StaticMockContext context = StaticMockContext.forClass(classToMock);
        context.setup();
        return new MockedStatic<>(classToMock, context);
    }

    public static void init(Object testInstance) {
        Class<?> clazz = testInstance.getClass();

        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Mock.class)) {
                    createMockForField(testInstance, field);
                }
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Spy.class)) {
                    createSpyForField(testInstance, field);
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

    private static void createSpyForField(Object testInstance, Field field) {
        try {
            field.setAccessible(true);

            Object realObject = field.get(testInstance);

            if (realObject == null) {
                throw new IllegalStateException(
                        "Field " + field.getName() + " annotated with @Spy is null. " +
                                "Please initialize it before calling initMocks()."
                );
            }

            Object spy = SpyFactory.createSpy(realObject);
            field.set(testInstance, spy);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create spy for field: " + field.getName(), e);
        }
    }
}