package ru.nsu.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpyApiTest {

    @Test
    void spyShouldDelegateToRealMethodWhenNoStubbing() {
        List<String> real = new ArrayList<>();
        real.add("a");
        real.add("b");

        List<String> spy = JokeMock.spy(real);

        assertEquals(2, spy.size());
        assertEquals("a", spy.get(0));
    }

    @Test
    void doReturnWhenSpyShouldOverrideMethodResult() {
        List<String> real = new ArrayList<>();
        real.add("x");

        List<String> spy = JokeMock.spy(real);

        JokeMock.doReturn(100).when(spy).size();

        assertEquals(100, spy.size());
        // остальные методы по умолчанию должны идти в реальный объект
        assertEquals("x", spy.get(0));
    }

    @Test
    void doThrowWhenSpyShouldThrowConfiguredException() {
        List<String> real = new ArrayList<>();
        List<String> spy = JokeMock.spy(real);

        JokeMock.doThrow(new IllegalStateException("boom")).when(spy).size();

        assertThrows(IllegalStateException.class, spy::size);
    }
}

