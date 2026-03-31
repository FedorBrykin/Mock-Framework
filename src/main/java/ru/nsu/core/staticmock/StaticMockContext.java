package ru.nsu.core.staticmock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class StaticMockContext {
    private static final Logger log = LoggerFactory.getLogger(StaticMockContext.class);
    private final Class<?> targetClass;
    private final Set<Class<?>> redefinedClasses = new HashSet<>();
    private boolean isActive = false;

    private StaticMockContext(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public static StaticMockContext forClass(Class<?> clazz) {
        StaticMockContext existing = StaticMockRegistry.getContext(clazz);
        if (existing != null) {
            return existing;
        }

        StaticMockContext context = new StaticMockContext(clazz);
        StaticMockRegistry.registerContext(clazz, context);
        return context;
    }

    public void setup() {
        if (!redefinedClasses.contains(targetClass) && !isActive) {
            if (log.isDebugEnabled()) {
                log.debug("Setting up static mock for class: {}", targetClass.getName());
            }
            StaticMockSupport.redefineForStaticMock(targetClass);
            redefinedClasses.add(targetClass);
            isActive = true;
            StaticMockRegistry.activate(targetClass);
        }
    }

    public void restore() {
        if (redefinedClasses.contains(targetClass) && isActive) {
            if (log.isDebugEnabled()) {
                log.debug("Restoring original class: {}", targetClass.getName());
            }
            StaticMockSupport.restoreOriginalClass(targetClass);
            redefinedClasses.remove(targetClass);
            isActive = false;
            StaticMockRegistry.deactivate(targetClass);
        }
    }

    public boolean isActive() {
        return isActive;
    }
}