package ru.nsu.core.progress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.core.invocation.Invocation;

import java.util.Arrays;
import java.util.Optional;

/**
 * Хранилище сопоставлений Invocation -> Answer
 * (в разработке)
 */
public class MockingProgress {
    private static final Logger log = LoggerFactory.getLogger(MockingProgress.class);

    private static final ThreadLocal<MockingProgress> INSTANCE =
            ThreadLocal.withInitial(MockingProgress::new);

    private final ThreadLocal<Invocation> lastRecordedInvocation = new ThreadLocal<>();

    private MockingProgress() {}

    public static MockingProgress getInstance() {
        return INSTANCE.get();
    }

    public void recordInvocation(Invocation invocation) {
        if (log.isDebugEnabled()) {
            log.debug("Recording invocation: {} args={}", invocation.getMethod(), Arrays.toString(invocation.getArgs()));
        }
        lastRecordedInvocation.set(invocation);
    }

    public Optional<Invocation> consumeLastRecordedInvocation() {
        Invocation invocation = lastRecordedInvocation.get();
        lastRecordedInvocation.remove();
        if (log.isDebugEnabled()) {
            log.debug("Consuming last recorded invocation: {}", invocation != null ? invocation.getMethod() : "null");
        }
        return Optional.ofNullable(invocation);
    }
}