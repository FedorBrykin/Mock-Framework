package ru.nsu.core.registy;

import ru.nsu.core.answer.Answer;
import ru.nsu.core.invocation.Invocation;
import ru.nsu.core.stub.Stubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class StubbingRegistry {
    private static final Logger log = LoggerFactory.getLogger(StubbingRegistry.class);
    private static final StubbingRegistry instance = new StubbingRegistry();
    private final List<Stubbing> stubbings = new CopyOnWriteArrayList<>();

    private StubbingRegistry() {}

    public static StubbingRegistry getInstance() {
        return instance;
    }

    public void addStubbing(Stubbing stubbing) {
        stubbings.add(0, stubbing); // добавляем в начало для приоритета последних
        if (log.isDebugEnabled()) {
            log.debug("Added stubbing for invocation: {}", stubbing.getInvocation());
        }
    }

    public Answer findAnswer(Invocation invocation) {
        for (Stubbing stubbing : stubbings) {
            if (stubbing.matches(invocation)) {
                Optional<Answer> answer = stubbing.getAnswer();
                if (answer.isPresent()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found answer for invocation: {}", invocation);
                    }
                    return answer.get();
                }
            }
        }
        return null;
    }

    public void removeStubbing(Invocation invocation) {
        stubbings.removeIf(stubbing -> stubbing.matches(invocation));
    }

    public void reset() {
        stubbings.clear();
        if (log.isDebugEnabled()) {
            log.debug("Stubbing registry reset");
        }
    }
}