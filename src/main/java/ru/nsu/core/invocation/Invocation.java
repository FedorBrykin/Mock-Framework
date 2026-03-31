package ru.nsu.core.invocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class Invocation {
    private final Object target;        // может быть null для статических вызовов
    private final Class<?> targetClass; // всегда заполнен (класс, где объявлен метод)
    private final Method method;
    private final Object[] args;

    // Конструктор для экземплярных вызовов
    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.targetClass = target != null ? target.getClass() : method.getDeclaringClass();
        this.method = method;
        this.args = args != null ? args : new Object[0];
    }

    // Конструктор для статических вызовов
    public Invocation(Class<?> targetClass, Method method, Object[] args) {
        this.target = null;
        this.targetClass = targetClass;
        this.method = method;
        this.args = args != null ? args : new Object[0];
    }

    public Object getTarget() {
        return target;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public boolean isStatic() {
        return target == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invocation that = (Invocation) o;
        return Objects.equals(targetClass, that.targetClass) &&
                Objects.equals(method, that.method) &&
                Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(targetClass, method);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return "Invocation{" +
                "targetClass=" + targetClass.getSimpleName() +
                ", method=" + method.getName() +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}