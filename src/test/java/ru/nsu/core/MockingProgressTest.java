package ru.nsu.core;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MockingProgressTest {

    static class SampleClass {
        public String echo(String s) {
            return s;
        }
    }

    @Test
    void recordAndConsumeInvocationShouldWork() throws NoSuchMethodException {
        MockingProgress progress = MockingProgress.getInstance();
        Method method = SampleClass.class.getMethod("echo", String.class);
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[]{"test"});

        progress.recordInvocation(invocation);
        Optional<Invocation> consumed = progress.consumeLastRecordedInvocation();
        Optional<Invocation> secondConsume = progress.consumeLastRecordedInvocation();

        assertTrue(consumed.isPresent());
        assertEquals(invocation, consumed.get());
        assertTrue(secondConsume.isEmpty(), "After consume the last call should be cleared");
    }

    @Test
    void addAndFindStubbingShouldReturnConfiguredAnswer() throws Throwable {
        MockingProgress progress = MockingProgress.getInstance();
        Method method = SampleClass.class.getMethod("echo", String.class);
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[]{"test"});
        Answer answer = Answer.returns("stubbed");

        progress.addStubbing(invocation, answer);

        Optional<Answer> found = progress.findAnswer(invocation);
        assertTrue(found.isPresent());
        assertEquals("stubbed", found.get().execute());
    }

    @Test
    void findAnswerShouldReturnEmptyForUnknownInvocation() throws NoSuchMethodException {
        MockingProgress progress = MockingProgress.getInstance();
        Method method = SampleClass.class.getMethod("echo", String.class);
        Invocation invocation = new Invocation(new SampleClass(), method, new Object[]{"another"});

        Optional<Answer> found = progress.findAnswer(invocation);

        assertTrue(found.isEmpty());
    }
}

