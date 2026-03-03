package ru.nsu.test.objects;

import ru.nsu.annotation.Mock;

public class UserService {

    public String getUserName(Long id) {
        return "User-" + id;
    }
}
