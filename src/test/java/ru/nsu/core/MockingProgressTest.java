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

    static class AnotherSampleClass {
        public String echo(String s) {
            return "another-" + s;
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
    void stubbingsShouldBeIndependentForDifferentObjects() throws Throwable {
        MockingProgress progress = MockingProgress.getInstance();

        Method methodSample = SampleClass.class.getMethod("echo", String.class);
        Method methodAnother = AnotherSampleClass.class.getMethod("echo", String.class);

        Invocation invocation1 = new Invocation(new SampleClass(), methodSample, new Object[]{"one"});
        Invocation invocation2 = new Invocation(new AnotherSampleClass(), methodAnother, new Object[]{"two"});

        Answer answer1 = Answer.returns("first");
        Answer answer2 = Answer.returns("second");

        progress.addStubbing(invocation1, answer1);
        progress.addStubbing(invocation2, answer2);

        Optional<Answer> found1 = progress.findAnswer(invocation1);
        Optional<Answer> found2 = progress.findAnswer(invocation2);

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals("first", found1.get().execute());
        assertEquals("second", found2.get().execute());
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

