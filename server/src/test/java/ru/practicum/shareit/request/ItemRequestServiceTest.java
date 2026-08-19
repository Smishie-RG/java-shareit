package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceTest {
    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Test
    void shouldCreateAndReturnRequestWithAnswer() {
        UserDto requestor = userService.create(
                new UserDto(null, "Автор", "author@example.com"));
        UserDto owner = userService.create(
                new UserDto(null, "Владелец", "owner@example.com"));

        ItemRequestDto request = requestService.create(
                requestor.getId(),
                new ItemRequestDto(null, "Нужна дрель", null, null));

        ItemDto item = itemService.create(
                owner.getId(),
                new ItemDto(null, "Дрель", "Ударная дрель", true,
                        request.getId()));

        ItemRequestDto savedRequest = requestService.getById(
                owner.getId(), request.getId());

        assertNotNull(savedRequest.getCreated());
        assertEquals("Нужна дрель", savedRequest.getDescription());
        assertEquals(1, savedRequest.getItems().size());
        assertEquals(item.getId(), savedRequest.getItems().get(0).getId());
        assertEquals(owner.getId(),
                savedRequest.getItems().get(0).getOwnerId());
    }

    @Test
    void shouldReturnOwnAndOtherRequests() {
        UserDto firstUser = userService.create(
                new UserDto(null, "Первый", "first@example.com"));
        UserDto secondUser = userService.create(
                new UserDto(null, "Второй", "second@example.com"));

        requestService.create(firstUser.getId(),
                new ItemRequestDto(null, "Первый запрос", null, null));
        requestService.create(firstUser.getId(),
                new ItemRequestDto(null, "Второй запрос", null, null));

        List<ItemRequestDto> ownRequests = requestService
                .getOwnRequests(firstUser.getId());
        List<ItemRequestDto> otherRequests = requestService
                .getAllRequests(secondUser.getId(), 0, 10);

        assertEquals(2, ownRequests.size());
        assertEquals("Второй запрос", ownRequests.get(0).getDescription());
        assertEquals(2, otherRequests.size());
    }

}
