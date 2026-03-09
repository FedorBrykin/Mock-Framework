package ru.nsu.core.registy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.core.answer.Answer;
import ru.nsu.core.exception.MockException;
import ru.nsu.core.invocation.Invocation;
import ru.nsu.core.stub.Stubbing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StubbingRegistry {
    private static final Logger log = LoggerFactory.getLogger(StubbingRegistry.class);

    private static final ThreadLocal<StubbingRegistry> INSTANCE =
            ThreadLocal.withInitial(StubbingRegistry::new);

    private final List<Stubbing> stubbings = new ArrayList<>();

    private StubbingRegistry() {}

    public static StubbingRegistry getInstance() {
        return INSTANCE.get();
    }

    public void reset() {
        stubbings.clear();
    }

    public Answer findAnswer(Invocation invocation) throws MockException {
        for (int i = stubbings.size() - 1; i >= 0; i--) {
            Stubbing stubbing = stubbings.get(i);
            if (stubbing.matches(invocation)) {
                Optional<Answer> answer = stubbing.getAnswer();
                if (answer.isEmpty()) {
                    throw new MockException("Нету мока для данного вызова");
                }
                if (log.isDebugEnabled()) {
                    log.debug("Find answer for method: {}", invocation.getMethod());
                }
                return answer.get();
            }
        }
        throw new MockException("Нету мока для данного вызова");
    }

    public void addStubbing(Stubbing stubbing) {
        stubbings.add(stubbing);
    }
}
