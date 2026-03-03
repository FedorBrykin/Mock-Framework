package ru.nsu.core.observer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.nsu.core.handler.InvocationHandler;
import ru.nsu.annotation.Mock;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class Observer {
    private static final Logger log = LoggerFactory.getLogger(Observer.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()
                .type(ElementMatchers.isAnnotatedWith(Mock.class)
                        .or(ElementMatchers.nameStartsWith("example.")))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) -> {
                    log.debug("Instrumenting class: {}", typeDescription.getSimpleName());

                    return builder.method(ElementMatchers.any())
                            .intercept(MethodDelegation.to(InvocationHandler.class));
                })
                .with(new AgentBuilder.Listener() {
                    @Override
                    public void onTransformation(TypeDescription typeDescription,
                                                 ClassLoader classLoader,
                                                 JavaModule module,
                                                 boolean loaded,
                                                 DynamicType dynamicType) {
                        log.debug("✓ Class {} ready for mocking", typeDescription.getSimpleName());
                    }

                    @Override
                    public void onError(String typeName,
                                        ClassLoader classLoader,
                                        JavaModule module,
                                        boolean loaded,
                                        Throwable throwable) {
                        log.debug("✗ Error processing {}: {}", typeName, throwable.getMessage());
                    }

                    @Override
                    public void onDiscovery(String typeName, ClassLoader classLoader,
                                            JavaModule module, boolean loaded) {
                        // Not needed for debugging
                    }

                    @Override
                    public void onIgnored(TypeDescription typeDescription,
                                          ClassLoader classLoader,
                                          JavaModule module, boolean loaded) {
                        // Not needed for debugging
                    }

                    @Override
                    public void onComplete(String typeName, ClassLoader classLoader,
                                           JavaModule module, boolean loaded) {
                        // Not needed for debugging
                    }
                })
                .installOn(inst);
    }
}