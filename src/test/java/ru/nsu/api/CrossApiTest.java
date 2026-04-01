package ru.nsu.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.nsu.annotation.Mock;
import ru.nsu.extension.JokeMockExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(JokeMockExtension.class)
class CrossApiTest {

    // Интерфейс для тестирования чистого мока
    interface Calculator {
        int add(int a, int b);
        int multiply(int a, int b);
    }

    @Mock
    private Calculator calculatorMock;

    @Test
    void mockAndSpyShouldWorkIndependently() {
        // 1. Настраиваем MOCK (через аннотацию)
        // Ожидаем, что mock не вызывает реальную логику (которой тут и нет), а возвращает заглушку
        JokeMock.when(calculatorMock.add(2, 2)).thenReturn(42);
        JokeMock.when(calculatorMock.multiply(2, 2)).thenReturn(100);

        // 2. Создаем SPY на реальном списке
        List<String> realList = new ArrayList<>();
        realList.add("real-item");
        List<String> spyList = JokeMock.spy(realList);

        // Переопределяем поведение size() для спая
        JokeMock.doReturn(999).when(spyList).size();

        // --- ПРОВЕРКИ ---

        // A. Проверяем, что Mock работает изолированно
        assertEquals(42, calculatorMock.add(2, 2));
        assertEquals(100, calculatorMock.multiply(2, 2));

        // B. Проверяем, что Spy работает изолированно
        // Метод size() переопределен
        assertEquals(999, spyList.size());
        // Метод get() делегируется реальному объекту
        assertEquals("real-item", spyList.get(0));

        // C. КРИТИЧЕСКАЯ ПРОВЕРКА: Убедимся, что действия со Spy не сломали Mock
        // (Состояние не должно пересекаться)
        assertEquals(42, calculatorMock.add(2, 2)); // Все еще 42, не изменилось

        // D. КРИТИЧЕСКАЯ ПРОВЕРКА: Убедимся, что действия с Mock не сломали Spy
        assertEquals(999, spyList.size()); // Все еще 999
        assertEquals(1, realList.size());  // Реальный объект внутри спая не пострадал (остался 1 элемент)
    }

    @Test
    void mixingStaticMockAndSpyOnSameTypeShouldNotConflict() {
        // Создаем еще один мок того же типа, что и поле, но через статический метод
        Calculator staticMock = JokeMock.mock(Calculator.class);
        JokeMock.when(staticMock.add(1, 1)).thenReturn(5);

        // Создаем спай
        List<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        List<Integer> spyNumbers = JokeMock.spy(numbers);
        JokeMock.doThrow(new RuntimeException("SpyBoom")).when(spyNumbers).clear();

        // Проверки
        assertEquals(5, staticMock.add(1, 1));
        assertEquals(10, spyNumbers.get(0)); // Реальный метод

        // Попытка вызвать clear() должна кинуть исключение только для спая
        assertThrows(RuntimeException.class, spyNumbers::clear);

        // Убедимся, что статический мок все еще жив
        assertEquals(5, staticMock.add(1, 1));
    }


    @Test
    void staticMockAndInstanceSpyShouldWorkIndependently() {
        // 1. Подготавливаем реальный объект для Spy
        List<String> realList = new ArrayList<>();
        realList.add("real");
        List<String> spyList = JokeMock.spy(realList);

        // Настраиваем Spy (переопределяем size)
        JokeMock.doReturn(999).when(spyList).size();

        // 2. Активируем Static Mock в блоке try-with-resources
        try (MockedStatic<StaticUtils> staticMock = JokeMock.mockStatic(StaticUtils.class)) {

            // Настраиваем статический метод
            staticMock.when(StaticUtils::name).thenReturn("StaticMockValue");
            staticMock.when(() -> StaticUtils.range(1, 3)).thenReturn(List.of(9, 8, 7));

            // --- ПРОВЕРКИ ВНУТРИ КОНТЕКСТА СТАТИЧЕСКОГО МОКА ---

            // A. Статический метод возвращает заглушку
            assertThat(StaticUtils.name()).isEqualTo("StaticMockValue");
            assertThat(StaticUtils.range(1, 3)).containsExactly(9, 8, 7);

            // B. Spy продолжает работать корректно (не сломан статическим моком)
            // size() должен возвращать переопределенное значение
            assertEquals(999, spyList.size());
            // get() должен идти в реальный объект
            assertEquals("real", spyList.get(0));

            // C. Обычный мок (если бы мы его создали здесь через JokeMock.mock(...))
            // тоже должен был бы работать независимо.
        }

        // --- ПРОВЕРКИ ПОСЛЕ ЗАВЕРШЕНИЯ СТАТИЧЕСКОГО МОКА ---

        // 1. Статический метод вернулся к реальному поведению
        assertThat(StaticUtils.name()).isEqualTo("Baeldung");
        assertThat(StaticUtils.range(1, 3)).containsExactly(1, 2);

        // 2. Spy все еще работает и не был сброшен выходом из блока static mock
        assertEquals(999, spyList.size());
        assertEquals("real", spyList.get(0));

        // 3. Реальный объект внутри спая не пострадал
        assertEquals(1, realList.size());
    }

    @Test
    void staticMockAndInstanceMockShouldNotInterfere() {
        // Создаем обычный мок интерфейса
        StaticUtils.GreetingService mockService = JokeMock.mock(StaticUtils.GreetingService.class);
        // Предположим, что в StaticUtils есть вложенный интерфейс или мы используем любой другой
        // Для примера используем список как мок-объект (упрощенно)
        List<String> mockList = JokeMock.mock(List.class);
        JokeMock.when(mockList.get(0)).thenReturn("MockedItem");

        try (MockedStatic<StaticUtils> staticMock = JokeMock.mockStatic(StaticUtils.class)) {
            staticMock.when(StaticUtils::name).thenReturn("OnlyStatic");

            // Статика работает
            assertEquals("OnlyStatic", StaticUtils.name());

            // Обычный мок работает независимо
            assertEquals("MockedItem", mockList.get(0));

            // Попытка вызвать реальный метод у мок-списка должна вернуть null или дефолт (зависит от реализации JokeMock)
            // Главное, что вызов статического метода не вызвал исключение в обычном моке.
        }

        // После закрытия static mock обычный мок все еще активен (если он не привязан к скоупу try)
        assertEquals("MockedItem", mockList.get(0));

        // Статика вернулась к реальности
        assertEquals("Baeldung", StaticUtils.name());
    }

}