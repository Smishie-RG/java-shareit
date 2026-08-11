package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public User add(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getById(long userId) {
        return users.get(userId);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findByEmail(String email) {
        for (User user : users.values()) {
            if (email.equals(user.getEmail())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void delete(long userId) {
        users.remove(userId);
    }
}