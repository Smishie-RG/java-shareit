package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Validated
@RestController
@RequestMapping("/requests")
public class ItemRequestController {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient requestClient;

    public ItemRequestController(ItemRequestClient requestClient) {
        this.requestClient = requestClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(USER_HEADER) @Positive long userId,
            @Valid @RequestBody ItemRequestDto requestDto) {
        return requestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(
            @RequestHeader(USER_HEADER) @Positive long userId) {
        return requestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader(USER_HEADER) @Positive long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader(USER_HEADER) @Positive long userId,
            @PathVariable @Positive long requestId) {
        return requestClient.getById(userId, requestId);
    }
}
