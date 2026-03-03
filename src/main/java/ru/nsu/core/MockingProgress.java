package ru.nsu.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище сопоставлений Invocation -> Answer
 *
 * (в разработке)
 */
public class MockingProgress {

    private static final MockingProgress INSTANCE = new MockingProgress();

    private final Map<Invocation, Answer> stubbings = new ConcurrentHashMap<>();
    private final ThreadLocal<Invocation> lastRecordedInvocation = new ThreadLocal<>();

    private MockingProgress() {
    }

    public static MockingProgress getInstance() {
        return INSTANCE;
    }

    /**
     * Сохранить последний вызов (будет исп. thenReturn/thenThrow).
     */
    public void recordInvocation(Invocation invocation) {
        lastRecordedInvocation.set(invocation);
    }

    /**
     * Забрать и очистить последний записанный вызов.
     */
    public Optional<Invocation> consumeLastRecordedInvocation() {
        Invocation invocation = lastRecordedInvocation.get();
        lastRecordedInvocation.remove();
        return Optional.ofNullable(invocation);
    }

    public void addStubbing(Invocation invocation, Answer answer) {
        stubbings.put(invocation, answer);
    }

    public Optional<Answer> findAnswer(Invocation invocation) {
        return Optional.ofNullable(stubbings.get(invocation));
    }
}

