package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeRequestDateAndItems() throws Exception {
        LocalDateTime created = LocalDateTime.of(2026, 8, 16, 12, 30);
        ItemRequestDto requestDto = new ItemRequestDto(
                1L,
                "Нужна дрель",
                created,
                List.of()
        );

        assertThat(json.write(requestDto))
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(json.write(requestDto))
                .extractingJsonPathStringValue("$.created")
                .isEqualTo("2026-08-16T12:30:00");
        assertThat(json.write(requestDto))
                .extractingJsonPathArrayValue("$.items")
                .isEmpty();
    }
}
