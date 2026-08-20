package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestItemDto;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest request,
                                                  List<Item> items) {
        List<ItemRequestItemDto> itemDtos = new ArrayList<>();
        for (Item item : items) {
            itemDtos.add(new ItemRequestItemDto(
                    item.getId(),
                    item.getName(),
                    item.getOwner().getId()
            ));
        }

        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                itemDtos
        );
    }
}
