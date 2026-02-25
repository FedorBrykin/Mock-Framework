package ru.nsu.core;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import ru.nsu.annotation.Mock;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@Slf4j

public class InvocationMatcher {
    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @This Object target,
                                   @AllArguments Object[] args,
                                   @SuperCall Callable<?> callable) throws Exception {
        Mock mockAnnotation = target.getClass().getAnnotation(Mock.class);
        if (mockAnnotation == null) {
            log.info("not a mock");
            return callable.call();
        }
        return null;
    }
}
