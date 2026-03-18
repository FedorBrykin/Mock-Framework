package ru.nsu.api;

import ru.nsu.core.answer.Answer;
import ru.nsu.core.handler.SpyHandler;

public class Stubber {

    private final Answer answer;

    Stubber(Answer answer) {
        this.answer = answer;
    }

    public <T> T when(T mockOrSpy) {
        SpyHandler.setPendingAnswer(answer);
        return mockOrSpy;
    }
}

