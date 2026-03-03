package ru.nsu.core;

/**
 * Описание поведения мока при вызове метода (вернуть значение или бросить исключение)
 */
public interface Answer {

    Object execute() throws Throwable;

    static Answer returns(Object value) {
        return () -> value;
    }

    static Answer throwsException(Throwable throwable) {
        return () -> {
            throw throwable;
        };
    }
}

