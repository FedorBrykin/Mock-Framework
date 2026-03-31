package ru.nsu.api;

import ru.nsu.core.answer.Answer;
import ru.nsu.core.handler.SpyHandler;
import ru.nsu.core.progress.MockingProgress;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Stubber {

    private final Answer answer;
    private final MockingProgress progress = MockingProgress.getInstance();

    Stubber(Answer answer) {
        this.answer = answer;
    }

    public <T> T when(T mockOrSpy) {
        // Для шпионов используем SpyHandler
        SpyHandler.setPendingAnswer(answer);
        // Для моков используем MockingProgress
        progress.setPendingAnswer(answer);
        return mockOrSpy;
    }

    // Альтернативный метод для явного указания метода без его вызова
    public <T> void when(Class<T> mockClass, String methodName, Object... args) {
        try {
            Method method = findMethod(mockClass, methodName, args);
            progress.setPendingAnswer(answer);
            // Здесь нужно создать специальную заглушку для метода
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup stubbing for method: " + methodName, e);
        }
    }

    private Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && areCompatible(method.getParameterTypes(), args)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Method not found: " + methodName);
    }

    private boolean areCompatible(Class<?>[] paramTypes, Object[] args) {
        if (paramTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }
}