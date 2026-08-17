package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDto {
    @Pattern(regexp = "(?s).*\\S.*", message = "Имя не может быть пустым")
    private String name;

    @Email(message = "Некорректный email")
    @Pattern(regexp = "(?s).*\\S.*", message = "Email не может быть пустым")
    private String email;
}
