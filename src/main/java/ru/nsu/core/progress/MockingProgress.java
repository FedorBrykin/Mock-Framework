package ru.nsu.core.progress;

import ru.nsu.core.answer.Answer;
import ru.nsu.core.invocation.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MockingProgress {
    private static final Logger log = LoggerFactory.getLogger(MockingProgress.class);
    private static final MockingProgress instance = new MockingProgress();
    private final ConcurrentLinkedDeque<Invocation> recordedInvocations = new ConcurrentLinkedDeque<>();
    private final ThreadLocal<Answer> pendingAnswer = new ThreadLocal<>();

    private MockingProgress() {}

    public static MockingProgress getInstance() {
        return instance;
    }

    public void recordInvocation(Invocation invocation) {
        recordedInvocations.addLast(invocation);
        if (log.isDebugEnabled()) {
            log.debug("Recorded invocation: {}", invocation);
        }
    }

    public Optional<Invocation> consumeLastRecordedInvocation() {
        Invocation invocation = recordedInvocations.pollLast();
        if (log.isDebugEnabled() && invocation != null) {
            log.debug("Consumed invocation: {}", invocation);
        }
        return Optional.ofNullable(invocation);
    }

    public void setPendingAnswer(Answer answer) {
        pendingAnswer.set(answer);
        if (log.isDebugEnabled()) {
            log.debug("Set pending answer: {}", answer);
        }
    }

    public Answer consumePendingAnswer() {
        Answer answer = pendingAnswer.get();
        pendingAnswer.remove();
        if (log.isDebugEnabled() && answer != null) {
            log.debug("Consumed pending answer: {}", answer);
        }
        return answer;
    }

    public void clear() {
        recordedInvocations.clear();
        pendingAnswer.remove();
        if (log.isDebugEnabled()) {
            log.debug("Mocking progress cleared");
        }
    }
}