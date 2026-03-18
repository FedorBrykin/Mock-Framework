package ru.nsu.core.handler;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.core.answer.Answer;
import ru.nsu.core.invocation.Invocation;
import ru.nsu.core.progress.MockingProgress;
import ru.nsu.core.registy.StubbingRegistry;
import ru.nsu.core.stub.Stubbing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class SpyHandler implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(SpyHandler.class);
    private static final StubbingRegistry registry = StubbingRegistry.getInstance();

    private final Object target;
    private static final ThreadLocal<Answer> pendingAnswer = new ThreadLocal<>();

    public SpyHandler(Object target) {
        this.target = target;
    }

    public static void setPendingAnswer(Answer answer) {
        pendingAnswer.set(answer);
    }

    public static Answer getPendingAnswer() {
        return pendingAnswer.get();
    }

    public static void clearPendingAnswer() {
        pendingAnswer.remove();
    }

    // Для ByteBuddy - экземплярный метод
    @RuntimeType
    public Object intercept(@Origin Method method,
                            @This Object mock,
                            @AllArguments Object[] args,
                            @SuperCall Callable<?> superCall) throws Throwable {
        if (isObjectMethod(method)) {
            return superCall.call();
        }
        return handle(mock, method, args);
    }

    // Для Proxy
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isObjectMethod(method)) {
            return method.invoke(this, args);
        }
        return handle(proxy, method, args);
    }

    private Object handle(Object mock, Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Spy intercepted call to {}.{}",
                    target.getClass().getSimpleName(), method.getName());
        }

        MockingProgress progress = MockingProgress.getInstance();
        Invocation invocation = new Invocation(mock, method, args);

        // Всегда записываем вызов (для when/doReturn)
        progress.recordInvocation(invocation);

        // Проверяем, есть ли ожидающий Answer от doReturn/doThrow
        Answer pending = pendingAnswer.get();
        if (pending != null) {
            if (log.isDebugEnabled()) {
                log.debug("Pending stub answer found for {}.{}",
                        target.getClass().getSimpleName(), method.getName());
            }
            Stubbing stubbing = new Stubbing(invocation);
            stubbing.thenAnswer(pending);
            registry.addStubbing(stubbing);
            clearPendingAnswer();
            return defaultValue(method.getReturnType());
        }

        // Ищем существующий стаббинг
        Answer answer = registry.findAnswer(invocation);
        if (answer != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found stubbing for {}.{}, executing",
                        target.getClass().getSimpleName(), method.getName());
            }
            return answer.execute();
        }

        // Если стаббинга нет, вызываем реальный метод на target
        if (log.isDebugEnabled()) {
            log.debug("No stubbing found for {}.{}, delegating to real object",
                    target.getClass().getSimpleName(), method.getName());
        }
        return method.invoke(target, args);
    }

    private boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == void.class) return null;
        if (!returnType.isPrimitive()) return null;
        if (returnType == boolean.class) return false;
        if (returnType == char.class) return '\0';
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0f;
        if (returnType == double.class) return 0d;
        return null;
    }
}