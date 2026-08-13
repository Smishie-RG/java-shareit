package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemServiceImpl(ItemStorage itemStorage,
                           UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        User owner = getUser(userId);
        validateNewItem(itemDto);

        Item item = ItemMapper.toItem(itemDto, owner, null);
        return ItemMapper.toItemDto(itemStorage.add(item));
    }

    @Override
    public ItemDto update(long userId,
                          long itemId,
                          ItemDto itemDto) {
        Item item = getItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    "Редактировать вещь может только владелец"
            );
        }

        if (itemDto.getName() != null) {
            validateText(
                    itemDto.getName(),
                    "Название не может быть пустым"
            );
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            validateText(
                    itemDto.getDescription(),
                    "Описание не может быть пустым"
            );
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemStorage.update(item));
    }

    @Override
    public ItemDto getById(long itemId) {
        return ItemMapper.toItemDto(getItem(itemId));
    }

    @Override
    public List<ItemDto> getAllByOwner(long userId) {
        getUser(userId);
        return toItemDtoList(itemStorage.getByOwnerId(userId));
    }

    @Override
    public List<ItemDto> search(String text) {
        return toItemDtoList(itemStorage.search(text));
    }

    private List<ItemDto> toItemDtoList(List<Item> items) {
        List<ItemDto> result = new ArrayList<>();

        for (Item item : items) {
            result.add(ItemMapper.toItemDto(item));
        }

        return result;
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

    private Item getItem(long itemId) {
        Item item = itemStorage.getById(itemId);

        if (item == null) {
            throw new NotFoundException(
                    "Вещь с id " + itemId + " не найдена"
            );
        }

        return item;
    }

    private void validateNewItem(ItemDto itemDto) {
        validateText(
                itemDto.getName(),
                "Название не может быть пустым"
        );
        validateText(
                itemDto.getDescription(),
                "Описание не может быть пустым"
        );

        if (itemDto.getAvailable() == null) {
            throw new ValidationException(
                    "Статус доступности должен быть указан"
            );
        }
    }

    private void validateText(String text, String message) {
        if (text == null || text.isBlank()) {
            throw new ValidationException(message);
        }
    }
}