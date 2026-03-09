package ru.nsu.core.invocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Вызов метода: объект, метод и аргументы
 * Исп. как ключ для поиска подходящего Answer
 */
public class Invocation {

    private final Object target;
    private final Method method;
    private final Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args != null ? args.clone() : new Object[0];
    }

    public Object getTarget() {
        return target;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invocation)) return false;
        Invocation that = (Invocation) o;
        return Objects.equals(method, that.method) &&
                Arrays.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(method);
        result = 31 * result + Arrays.deepHashCode(args);
        return result;
    }
}

