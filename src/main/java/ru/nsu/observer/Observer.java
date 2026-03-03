package ru.nsu.observer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import ru.nsu.core.InvocationMatcher;
import ru.nsu.annotation.Mock;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Observer {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("=== Запуск Mocking Agent ===");

        new AgentBuilder.Default()
                .type(ElementMatchers.isAnnotatedWith(Mock.class)
                        .or(ElementMatchers.nameStartsWith("example.")))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
                        System.out.println("Инструментируем класс: " + typeDescription.getSimpleName());

                        return builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(InvocationMatcher.class));
                    }
                })
                .with(new AgentBuilder.Listener() {
                    @Override
                    public void onTransformation(TypeDescription typeDescription,
                                                 ClassLoader classLoader,
                                                 JavaModule module,
                                                 boolean loaded,
                                                 DynamicType dynamicType) {
                        System.out.println("✓ Класс " + typeDescription.getSimpleName() +
                                " готов к мокированию");
                    }

                    @Override
                    public void onError(String typeName,
                                        ClassLoader classLoader,
                                        JavaModule module,
                                        boolean loaded,
                                        Throwable throwable) {
                        System.err.println("✗ Ошибка при обработке " + typeName +
                                ": " + throwable.getMessage());
                    }

                    // Остальные методы слушателя...
                    public void onDiscovery(String typeName, ClassLoader classLoader,
                                            JavaModule module, boolean loaded) {}
                    public void onIgnored(TypeDescription typeDescription,
                                          ClassLoader classLoader,
                                          JavaModule module, boolean loaded) {}
                    public void onComplete(String typeName, ClassLoader classLoader,
                                           JavaModule module, boolean loaded) {}
                })
                .installOn(inst);
    }
}
