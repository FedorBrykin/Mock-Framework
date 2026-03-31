package ru.nsu.api;

import ru.nsu.core.staticmock.StaticMockContext;

import java.util.function.Supplier;

public class MockedStatic<T> implements AutoCloseable {
    private final Class<T> targetClass;
    private final StaticMockContext context;

    MockedStatic(Class<T> targetClass, StaticMockContext context) {
        this.targetClass = targetClass;
        this.context = context;
    }

    // Для методов, возвращающих значение
    public <R> OngoingStubbing<R> when(Supplier<R> methodCall) {
        try {
            methodCall.get();
        } catch (Exception e) {
            // Если метод выбросил исключение, это нормально для when
            // Мы просто записали invocation
        }
        return new OngoingStubbing<>();
    }

    // Для void методов
    public OngoingStubbing<Void> when(ThrowingRunnable methodCall) {
        try {
            methodCall.run();
        } catch (Throwable e) {
            // Исключение может быть выброшено, если метод заглушен
        }
        return new OngoingStubbing<>();
    }

    @Override
    public void close() {
        context.restore();
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
}