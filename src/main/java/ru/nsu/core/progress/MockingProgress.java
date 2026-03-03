package ru.nsu.core.progress;

import ru.nsu.core.answer.Answer;
import ru.nsu.core.invocation.Invocation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Хранилище сопоставлений Invocation -> Answer
 * <p>
 * (в разработке)
 */
public class MockingProgress {
    private static final Logger log = LoggerFactory.getLogger(MockingProgress.class);

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
        if (log.isDebugEnabled()) {
            log.debug("Recording invocation: {} args={}", invocation.getMethod(), java.util.Arrays.toString(invocation.getArgs()));
        }
        lastRecordedInvocation.set(invocation);
    }

    /**
     * Забрать и очистить последний записанный вызов.
     */
    public Optional<Invocation> consumeLastRecordedInvocation() {
        Invocation invocation = lastRecordedInvocation.get();
        lastRecordedInvocation.remove();
        if (log.isDebugEnabled()) {
            log.debug("Consuming last recorded invocation: {}", invocation != null ? invocation.getMethod() : "null");
        }
        return Optional.ofNullable(invocation);
    }

    public void addStubbing(Invocation invocation, Answer answer) {
        if (log.isDebugEnabled()) {
            log.debug("Adding stubbing for method: {}", invocation.getMethod());
        }
        stubbings.put(invocation, answer);
    }

    public Optional<Answer> findAnswer(Invocation invocation) {
        Answer answer = stubbings.get(invocation);
        if (log.isDebugEnabled()) {
            log.debug("Finding answer for method: {} -> {}", invocation.getMethod(), answer != null ? "found" : "not found");
        }
        return Optional.ofNullable(answer);
    }
}
