package ru.nsu.core.staticmock;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import ru.nsu.core.answer.Answer;
import ru.nsu.core.exception.MockException;
import ru.nsu.core.invocation.Invocation;
import ru.nsu.core.progress.MockingProgress;
import ru.nsu.core.registy.StubbingRegistry;
import ru.nsu.core.stub.Stubbing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.bytebuddy.matcher.ElementMatchers.*;

public final class StaticMockSupport {
    private static final Logger log = LoggerFactory.getLogger(StaticMockSupport.class);

    private StaticMockSupport() {}

    public static void redefineForStaticMock(Class<?> targetClass) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Redefining class for static mock: {}", targetClass.getName());
            }

            new ByteBuddy()
                    .redefine(targetClass, ClassFileLocator.ForClassLoader.of(targetClass.getClassLoader()))
                    .visit(Advice.to(StaticMethodAdvice.class)
                            .on(isStatic()
                                    .and(not(isTypeInitializer()))
                                    .and(not(isSynthetic()))
                                    .and(not(isAbstract()))))
                    .make()
                    .load(
                            targetClass.getClassLoader(),
                            ClassReloadingStrategy.of(StaticMockAgent.getInstrumentation()));

            if (log.isDebugEnabled()) {
                log.debug("Successfully redefined class: {}", targetClass.getName());
            }
        } catch (Exception e) {
            throw new MockException("Failed to redefine static mock class for " + targetClass.getName(), e);
        }
    }

    public static void restoreOriginalClass(Class<?> targetClass) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Restoring original class: {}", targetClass.getName());
            }

            new ByteBuddy()
                    .redefine(targetClass, ClassFileLocator.ForClassLoader.of(targetClass.getClassLoader()))
                    .make()
                    .load(
                            targetClass.getClassLoader(),
                            ClassReloadingStrategy.of(StaticMockAgent.getInstrumentation()));

            if (log.isDebugEnabled()) {
                log.debug("Successfully restored class: {}", targetClass.getName());
            }
        } catch (Exception e) {
            throw new MockException("Failed to restore original class for " + targetClass.getName(), e);
        }
    }

    public static InterceptDecision handleStaticCall(
            Class<?> targetClass,
            Method method,
            Object[] args
    ) throws Throwable {
        Object[] safeArgs = args != null ? args : new Object[0];
        Invocation invocation = new Invocation(targetClass, method, safeArgs);

        if (log.isDebugEnabled()) {
            log.debug("Handling static call: {}.{}", targetClass.getSimpleName(), method.getName());
        }

        MockingProgress progress = MockingProgress.getInstance();
        StubbingRegistry registry = StubbingRegistry.getInstance();

        // Регистрируем вызов для when()
        progress.recordInvocation(invocation);
        StaticMockRegistry.registerInvocation(targetClass, invocation);

        // Проверяем, есть ли ожидающий Answer от doReturn/doThrow
        Answer pendingAnswer = progress.consumePendingAnswer();
        if (pendingAnswer != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found pending answer for static method: {}.{}",
                        targetClass.getSimpleName(), method.getName());
            }
            Stubbing stubbing = new Stubbing(invocation);
            stubbing.thenAnswer(pendingAnswer);
            registry.addStubbing(stubbing);
            return InterceptDecision.skipWithReturn(pendingAnswer.execute());
        }

        // Ищем существующий стаббинг
        Answer answer = registry.findAnswer(invocation);
        if (answer != null) {
            if (log.isDebugEnabled()) {
                log.debug("Found stubbing for static method: {}.{}",
                        targetClass.getSimpleName(), method.getName());
            }
            return InterceptDecision.skipWithReturn(answer.execute());
        }

        // Если нет стаббинга и мок активен - возвращаем значение по умолчанию
        if (StaticMockRegistry.isActive(targetClass)) {
            if (log.isDebugEnabled()) {
                log.debug("No stubbing found for static method: {}.{}, returning default value",
                        targetClass.getSimpleName(), method.getName());
            }
            return InterceptDecision.skipWithReturn(getDefaultValue(method.getReturnType()));
        }

        // Иначе вызываем оригинальный метод
        if (log.isDebugEnabled()) {
            log.debug("Proceeding with original static method: {}.{}",
                    targetClass.getSimpleName(), method.getName());
        }
        return InterceptDecision.proceed();
    }

    static Method resolveMethod(Class<?> targetClass, String methodName, Object[] args) {
        List<Method> candidates = new ArrayList<>();
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && isCompatible(method, args)) {
                candidates.add(method);
            }
        }

        if (candidates.isEmpty()) {
            throw new MockException(
                    "Static method not found: " + methodName
                            + " compatible with args " + Arrays.toString(args) + " on " + targetClass.getName());
        }

        if (candidates.size() > 1) {
            throw new MockException(
                    "Ambiguous static method call: " + methodName
                            + " with args " + Arrays.toString(args) + " matches multiple candidates: " + candidates);
        }

        return candidates.get(0);
    }

    private static boolean isCompatible(Method method, Object[] args) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != args.length) {
            return false;
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (args[i] != null && !parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    static Object getDefaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) return false;
        if (returnType == char.class) return '\u0000';
        if (returnType == byte.class) return (byte) 0;
        if (returnType == short.class) return (short) 0;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == float.class) return 0.0f;
        if (returnType == double.class) return 0.0d;
        return null;
    }

    public static final class InterceptDecision {
        public final boolean skipOriginal;
        public final Object returnValue;
        public final Throwable throwable;

        private InterceptDecision(boolean skipOriginal, Object returnValue, Throwable throwable) {
            this.skipOriginal = skipOriginal;
            this.returnValue = returnValue;
            this.throwable = throwable;
        }

        public static InterceptDecision proceed() {
            return new InterceptDecision(false, null, null);
        }

        public static InterceptDecision skipWithReturn(Object value) {
            return new InterceptDecision(true, value, null);
        }

        public static InterceptDecision skipWithThrowable(Throwable throwable) {
            return new InterceptDecision(true, null, throwable);
        }
    }

    public static class StaticMethodAdvice {

        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(
                @Advice.Origin Class<?> targetClass,
                @Advice.Origin Method method,
                @Advice.AllArguments Object[] args,
                @Advice.Local("decision") InterceptDecision decision
        ) throws Throwable {
            if (!StaticMockRegistry.isActive(targetClass)) {
                decision = InterceptDecision.proceed();
                return false;
            }

            decision = handleStaticCall(targetClass, method, args);
            return decision.skipOriginal;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(
                @Advice.Enter boolean skipped,
                @Advice.Local("decision") InterceptDecision decision,
                @Advice.Return(readOnly = false, typing = net.bytebuddy.implementation.bytecode.assign.Assigner.Typing.DYNAMIC) Object returned,
                @Advice.Thrown(readOnly = false) Throwable thrown
        ) throws Throwable {
            if (!skipped || decision == null) {
                return;
            }

            if (decision.throwable != null) {
                thrown = decision.throwable;
                return;
            }

            returned = decision.returnValue;
            thrown = null;
        }
    }
}