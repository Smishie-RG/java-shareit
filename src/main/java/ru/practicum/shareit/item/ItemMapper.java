package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        Long requestId = null;

        if (item.getRequest() != null) {
            requestId = item.getRequest().getId();
        }

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId
        );
    }

    public static Item toItem(ItemDto itemDto,
                              User owner,
                              ItemRequest request) {
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                request
        );
    }

    public static ItemResponseDto toItemResponseDto(
            Item item,
            BookingShortDto lastBooking,
            BookingShortDto nextBooking,
            List<CommentDto> comments) {
        Long requestId = null;
        if (item.getRequest() != null) {
            requestId = item.getRequest().getId();
        }

        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId,
                lastBooking,
                nextBooking,
                comments
        );
    }
}
