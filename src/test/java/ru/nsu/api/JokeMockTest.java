package ru.nsu.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JokeMockTest {

    interface GreetingService {
        String greet(String name);
    }

    @Test
    void whenThenReturnShouldReturnStubbedValueForInterfaceMock() {
        GreetingService mock = JokeMock.mock(GreetingService.class);

        JokeMock.when(() -> mock.greet("John"))
                .thenReturn("Hi John");

        String result = mock.greet("John");

        assertEquals("Hi John", result);
    }
}

