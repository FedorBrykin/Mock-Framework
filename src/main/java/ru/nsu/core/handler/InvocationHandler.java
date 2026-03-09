// ru.nsu.core.handler.InvocationHandler.java
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
import ru.nsu.core.registy.StubbingRegistry;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(InvocationHandler.class);
    private static final StubbingRegistry registry = StubbingRegistry.getInstance();

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @This Object mock,
                                   @AllArguments Object[] args,
                                   @SuperCall Callable<?> superCall) throws Throwable {
        if (isObjectMethod(method)) {
            if (log.isDebugEnabled()) {
                log.debug("Intercepted call to Object method");
            }
            return superCall.call();
        }
        return handle(mock, method, args);
    }

    public static Object handle(Object mock, Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Intercepted call to {}.{} on mock {}",
                    mock.getClass().getSimpleName(), method.getName(),
                    System.identityHashCode(mock));
        }

        Answer answer = registry.findAnswer(new Invocation(mock, method, args));
        if (log.isDebugEnabled()) {
            log.debug("Found stubbing for {}.{}, executing",
                    mock.getClass().getSimpleName(), method.getName());
        }
        return answer.execute();
    }

    private static boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }
}