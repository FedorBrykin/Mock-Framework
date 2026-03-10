package ru.nsu.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.nsu.annotation.Mock;
import ru.nsu.extension.JokeMockExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JokeMockExtension.class)
class JokeMockTest {

    interface GreetingService {
        String greet(String name);
    }

    @Mock
    private GreetingService greetingService;

    @Test
    void whenThenReturnShouldReturnStubbedValueForInterfaceMock() {
        GreetingService mock = JokeMock.mock(GreetingService.class);

        JokeMock.when(() -> mock.greet("John"))
                .thenReturn("Hi John");

        String result = mock.greet("John");

        assertEquals("Hi John", result);
    }

    @Test
    void extensionShouldInitializeMockAnnotatedField() {
        JokeMock.when(() -> greetingService.greet("Kate"))
                .thenReturn("Hello Kate");

        String result = greetingService.greet("Kate");

        assertEquals("Hello Kate", result);
    }
}

