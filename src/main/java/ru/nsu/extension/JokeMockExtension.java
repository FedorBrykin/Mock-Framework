package ru.nsu.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import ru.nsu.api.JokeMock;

public class JokeMockExtension implements TestInstancePostProcessor,
        BeforeEachCallback,
        AfterEachCallback {

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        JokeMock.init(testInstance);
    }

    @Override
    public void beforeEach(ExtensionContext context) {}

    @Override
    public void afterEach(ExtensionContext context) {
        JokeMock.reset();
    }
}
