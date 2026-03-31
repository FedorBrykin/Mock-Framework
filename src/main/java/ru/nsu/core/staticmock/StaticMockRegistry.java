package ru.nsu.core.staticmock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class StaticMockRegistry {
    private static final Logger log = LoggerFactory.getLogger(StaticMockRegistry.class);
    private static final Set<Class<?>> activeStaticMocks = new CopyOnWriteArraySet<>();
    private static final ConcurrentMap<Class<?>, StaticMockContext> contexts = new ConcurrentHashMap<>();

    public static void activate(Class<?> targetClass) {
        activeStaticMocks.add(targetClass);
        if (log.isDebugEnabled()) {
            log.debug("Activated static mock for class: {}", targetClass.getName());
        }
    }

    public static void deactivate(Class<?> targetClass) {
        activeStaticMocks.remove(targetClass);
        contexts.remove(targetClass);
        if (log.isDebugEnabled()) {
            log.debug("Deactivated static mock for class: {}", targetClass.getName());
        }
    }

    public static boolean isActive(Class<?> targetClass) {
        return activeStaticMocks.contains(targetClass);
    }

    public static void registerInvocation(Class<?> targetClass, ru.nsu.core.invocation.Invocation invocation) {
        if (isActive(targetClass) && log.isDebugEnabled()) {
            log.debug("Registered static invocation for {}: {}", targetClass.getSimpleName(), invocation);
        }
    }

    public static void registerContext(Class<?> targetClass, StaticMockContext context) {
        contexts.put(targetClass, context);
    }

    public static StaticMockContext getContext(Class<?> targetClass) {
        return contexts.get(targetClass);
    }

    public static void clear() {
        activeStaticMocks.clear();
        contexts.clear();
        if (log.isDebugEnabled()) {
            log.debug("Static mock registry cleared");
        }
    }
}