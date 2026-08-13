package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {
    User add(User user);

    User update(User user);

    User getById(long userId);

    List<User> getAll();

    User findByEmail(String email);

    void delete(long userId);
}