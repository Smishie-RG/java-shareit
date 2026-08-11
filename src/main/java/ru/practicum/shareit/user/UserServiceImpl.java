package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateName(userDto.getName());
        validateEmail(userDto.getEmail());
        checkEmail(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        return UserMapper.toUserDto(userStorage.add(user));
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User user = getUser(userId);

        if (userDto.getName() != null) {
            validateName(userDto.getName());
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            checkEmail(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(userStorage.update(user));
    }

    @Override
    public UserDto getById(long userId) {
        return UserMapper.toUserDto(getUser(userId));
    }

    @Override
    public List<UserDto> getAll() {
        List<UserDto> result = new ArrayList<>();
        for (User user : userStorage.getAll()) {
            result.add(UserMapper.toUserDto(user));
        }
        return result;
    }

    @Override
    public void delete(long userId) {
        getUser(userId);
        userStorage.delete(userId);
    }

    private User getUser(long userId) {
        User user = userStorage.getById(userId);
        if (user == null) {
            throw new NotFoundException(
                    "Пользователь с id " + userId + " не найден"
            );
        }
        return user;
    }

    private void checkEmail(String email, Long userId) {
        User user = userStorage.findByEmail(email);
        if (user != null && !user.getId().equals(userId)) {
            throw new EmailAlreadyExistsException("Email уже используется");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Имя не может быть пустым");
        }
    }

    private void validateEmail(String email) {
        if (email == null
                || email.isBlank()
                || !email.matches("^[^@\\s]+@[^@\\s]+$")) {
            throw new ValidationException("Некорректный email");
        }
    }
}