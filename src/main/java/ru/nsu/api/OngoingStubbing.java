package ru.nsu.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.core.answer.Answer;
import ru.nsu.core.invocation.Invocation;
import ru.nsu.core.progress.MockingProgress;
import ru.nsu.core.registy.StubbingRegistry;
import ru.nsu.core.stub.Stubbing;

import java.util.Optional;

public class OngoingStubbing<T> {

    private static final Logger log = LoggerFactory.getLogger(OngoingStubbing.class);

    private final MockingProgress progress = MockingProgress.getInstance();
    private final StubbingRegistry registry = StubbingRegistry.getInstance();

    public void thenReturn(T value) {
        addAnswer(Answer.returns(value));
    }

    public void thenThrow(Throwable throwable) {
        addAnswer(Answer.throwsException(throwable));
    }

    private void addAnswer(Answer answer) {
        Optional<Invocation> recorded = progress.consumeLastRecordedInvocation();
        if (recorded.isEmpty()) {
            throw new IllegalStateException("There is no recorded call for setting up stubbing. " +
                    "Make sure you call the mock method inside when().");
        }
        Invocation invocation = recorded.get();

        Stubbing stubbing = new Stubbing(invocation);
        stubbing.thenAnswer(answer);
        registry.addStubbing(stubbing);

        if (log.isDebugEnabled()) {
            log.debug("Added stubbing for method: {}", invocation.getMethod());
        }
    }
}