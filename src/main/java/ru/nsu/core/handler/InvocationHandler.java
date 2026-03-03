package ru.nsu.core.handler;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.core.answer.Answer;
import ru.nsu.core.invocation.Invocation;
import ru.nsu.core.registy.StubbingRegistry;

import java.lang.reflect.Method;

public class InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(InvocationHandler.class);
    private static final StubbingRegistry registry = StubbingRegistry.getInstance();

    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @This Object target,
                                   @AllArguments Object[] args) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Intercepted call to {}.{}",
                    target.getClass().getSimpleName(), method.getName());
        }

        Invocation invocation = new Invocation(target, method, args);

        Answer answer = registry.findAnswer(invocation);

        if (log.isDebugEnabled()) {
            log.debug("Found stubbing for {}.{}, executing",
                    target.getClass().getSimpleName(), method.getName());
        }
        return answer.execute();
    }
}
