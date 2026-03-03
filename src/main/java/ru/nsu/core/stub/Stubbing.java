package ru.nsu.core.stub;

import ru.nsu.core.answer.Answer;
import ru.nsu.core.invocation.Invocation;

import java.util.Optional;

public class Stubbing {
    private Optional<Answer> answer;
    private final Invocation invocation;

    public Stubbing(Invocation invocation) {
        this.invocation = invocation;
        this.answer = Optional.empty();
    }

    public void thenAnswer(Answer answer) {
        this.answer = Optional.ofNullable(answer);
    }

    public Optional<Answer> getAnswer() {
        return answer;
    }

    public boolean matches(Invocation invocation) {
        if (invocation == null) {
            return false;
        }
        return this.invocation.equals(invocation);
    }
}