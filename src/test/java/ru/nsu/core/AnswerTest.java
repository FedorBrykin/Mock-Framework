package ru.nsu.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnswerTest {

    @Test
    void returnsShouldWrapValue() throws Throwable {
        Answer answer = Answer.returns("hello");

        Object result = answer.execute();

        assertEquals("hello", result);
    }

    @Test
    void throwsExceptionShouldThrowWrappedThrowable() {
        RuntimeException ex = new RuntimeException("crack");
        Answer answer = Answer.throwsException(ex);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            try {
                answer.execute();
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new RuntimeException(t);
            }
        });

        assertSame(ex, thrown);
    }
}

