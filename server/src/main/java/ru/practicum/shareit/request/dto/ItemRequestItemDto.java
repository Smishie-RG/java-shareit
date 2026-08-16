package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemRequestItemDto {
    private Long id;
    private String name;
    private Long ownerId;
}
