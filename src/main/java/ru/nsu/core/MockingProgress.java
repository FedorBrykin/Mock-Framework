package ru.nsu.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Хранилище сопоставлений Invocation -> Answer
 *
 * (в разработке)
 */
public class MockingProgress {

    private static final Logger LOG = Logger.getLogger(MockingProgress.class.getName());

    private static final ThreadLocal<MockingProgress> INSTANCE =
            ThreadLocal.withInitial(MockingProgress::new);

    private final Map<Invocation, Answer> stubbings = new ConcurrentHashMap<>();
    private final ThreadLocal<Invocation> lastRecordedInvocation = new ThreadLocal<>();

    private MockingProgress() {
    }

    public static MockingProgress getInstance() {
        return INSTANCE.get();
    }

    /**
     * Сохранить последний вызов (будет исп. thenReturn/thenThrow).
     */
    public void recordInvocation(Invocation invocation) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Recording invocation: " + invocation.getMethod() +
                    " args=" + java.util.Arrays.toString(invocation.getArgs()));
        }
        lastRecordedInvocation.set(invocation);
    }

    /**
     * Забрать и очистить последний записанный вызов.
     */
    public Optional<Invocation> consumeLastRecordedInvocation() {
        Invocation invocation = lastRecordedInvocation.get();
        lastRecordedInvocation.remove();
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Consuming last recorded invocation: " +
                    (invocation != null ? invocation.getMethod() : "null"));
        }
        return Optional.ofNullable(invocation);
    }

    public void addStubbing(Invocation invocation, Answer answer) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Adding stubbing for method: " + invocation.getMethod());
        }
        stubbings.put(invocation, answer);
    }

    public Optional<Answer> findAnswer(Invocation invocation) {
        Answer answer = stubbings.get(invocation);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Finding answer for method: " + invocation.getMethod() +
                    " -> " + (answer != null ? "found" : "not found"));
        }
        return Optional.ofNullable(answer);
    }
}
