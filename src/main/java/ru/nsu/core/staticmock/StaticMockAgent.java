package ru.nsu.core.staticmock;

import java.lang.instrument.Instrumentation;

public final class StaticMockAgent {
    private static volatile Instrumentation instrumentation;

    private StaticMockAgent() {}

    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("[JokeMock] StaticMockAgent installed successfully");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("[JokeMock] StaticMockAgent attached dynamically");
    }

    public static Instrumentation getInstrumentation() {
        if (instrumentation == null) {
            throw new IllegalStateException(
                    "Static mock agent is not installed. " +
                            "Run tests with: -javaagent:path/to/joke-mock-agent.jar"
            );
        }
        return instrumentation;
    }
}