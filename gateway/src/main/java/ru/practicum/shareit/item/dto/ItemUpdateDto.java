package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemUpdateDto {
    @Pattern(regexp = "(?s).*\\S.*", message = "Название не может быть пустым")
    private String name;

    @Pattern(regexp = "(?s).*\\S.*", message = "Описание не может быть пустым")
    private String description;

    private Boolean available;
}
