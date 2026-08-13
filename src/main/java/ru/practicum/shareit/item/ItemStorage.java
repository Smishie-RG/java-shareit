package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item add(Item item);

    Item update(Item item);

    Item getById(long itemId);

    List<Item> getByOwnerId(long ownerId);

    List<Item> search(String text);
}