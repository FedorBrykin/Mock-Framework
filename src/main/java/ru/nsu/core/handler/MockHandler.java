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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class MockHandler implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(MockHandler.class);
    private static final StubbingRegistry registry = StubbingRegistry.getInstance();

    // Для ByteBuddy (классы)
    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @This Object mock,
                                   @AllArguments Object[] args,
                                   @SuperCall Callable<?> superCall) throws Throwable {
        if (isObjectMethod(method)) {
            return superCall.call();
        }
        return handle(mock, method, args);
    }

    // Для Proxy (интерфейсы)
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isObjectMethod(method)) {
            return method.invoke(this, args);
        }
        return handle(proxy, method, args);
    }

    private static Object handle(Object mock, Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Intercepted call to {}.{} on mock {}",
                    mock.getClass().getSimpleName(), method.getName(),
                    System.identityHashCode(mock));
        }

        MockingProgress progress = MockingProgress.getInstance();
        Invocation invocation = new Invocation(mock, method, args);

        // Всегда записываем последний вызов (для when)
        progress.recordInvocation(invocation);

        // Ищем стаббинг
        Answer answer = registry.findAnswer(invocation);
        if (answer != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found stubbing for {}.{}, executing",
                        mock.getClass().getSimpleName(), method.getName());
            }
            return answer.execute();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No stubbing found for {}.{}, returning default value",
                        mock.getClass().getSimpleName(), method.getName());
            }
            // Возвращаем значение по умолчанию
            return null;
        }
    }

    private static boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }
}