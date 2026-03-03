package ru.nsu.test.objects;

import ru.nsu.annotation.Mock;

@Mock
public class OrderService  {

    public Double calculateTotal(double price, int quantity) {
        return price * quantity;
    }
}
