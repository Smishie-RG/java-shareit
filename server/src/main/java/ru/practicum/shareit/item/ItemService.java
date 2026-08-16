package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long userId, long itemId, ItemDto itemDto);

    ItemResponseDto getById(long userId, long itemId);

    List<ItemResponseDto> getAllByOwner(long userId, int from, int size);

    List<ItemDto> search(long userId, String text, int from, int size);

    CommentDto addComment(long userId, long itemId, CommentDto commentDto);
}
