package ru.nsu;

import ru.nsu.test.objects.OrderService;
import ru.nsu.test.objects.UserService;

public class Application {
    public static void main(String[] args) {
        System.out.println("=== Запуск приложения ===");

        UserService userService = new UserService();
        OrderService orderService = new OrderService();

        // Вызовы методов будут автоматически логироваться
        String userName = userService.getUserName(123L);
        System.out.println(userName);

        var total = orderService.calculateTotal(100.50, 3);
        System.out.println(total);

        System.out.println("=== Приложение завершено ===");
    }
}