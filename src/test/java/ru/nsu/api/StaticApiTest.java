package ru.nsu.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StaticApiTest {
    @Test
    void givenStaticMethodWithNoArgs_whenMocked_thenReturnsMockSuccessfully() {
        assertThat(StaticUtils.name()).isEqualTo("Baeldung");

        try (MockedStatic<StaticUtils> utilities = JokeMock.mockStatic(StaticUtils.class)) {
            utilities.when(StaticUtils::name).thenReturn("Eugen");
            assertThat(StaticUtils.name()).isEqualTo("Eugen");
        }

        assertThat(StaticUtils.name()).isEqualTo("Baeldung");
    }

    @Test
    void givenStaticMethodWithArgs_whenMocked_thenReturnsMockSuccessfully() {
        assertThat(StaticUtils.range(1, 5)).containsExactly(1, 2, 3, 4);

        try (MockedStatic<StaticUtils> utilities = JokeMock.mockStatic(StaticUtils.class)) {
            utilities.when(() -> StaticUtils.range(1, 5)).thenReturn(java.util.List.of(10, 20, 30, 40));
            assertThat(StaticUtils.range(1, 5)).containsExactly(10, 20, 30, 40);
        }

        assertThat(StaticUtils.range(1, 5)).containsExactly(1, 2, 3, 4);
    }
}
