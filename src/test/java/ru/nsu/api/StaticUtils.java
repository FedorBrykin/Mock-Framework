package ru.nsu.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StaticUtils {
    // Вспомогательный интерфейс для примера, если его нет в StaticUtils
    interface GreetingService {
        String greet(String name);
    }

    public static List<Integer> range(int start, int end) {
        return IntStream.range(start, end)
                .boxed()
                .collect(Collectors.toList());
    }

    public static String name() {
        return "Baeldung";
    }
}

