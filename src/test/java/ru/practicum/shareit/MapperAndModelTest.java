package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MapperAndModelTest {
    @Test
    void shouldMapUserInBothDirections() {
        User user = new User(
                1L,
                "Иван",
                "ivan@mail.ru"
        );

        UserDto dto = UserMapper.toUserDto(user);
        User mappedUser = UserMapper.toUser(dto);

        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(dto.getId(), mappedUser.getId());
        assertEquals(dto.getName(), mappedUser.getName());
        assertEquals(dto.getEmail(), mappedUser.getEmail());
    }

    @Test
    void shouldMapItemWithRequest() {
        User owner = new User(
                1L,
                "Владелец",
                "owner@mail.ru"
        );

        ItemRequest request = new ItemRequest(
                10L,
                "Нужна дрель",
                owner,
                LocalDateTime.now()
        );

        Item item = new Item(
                2L,
                "Дрель",
                "Ударная",
                true,
                owner,
                request
        );

        ItemDto dto = ItemMapper.toItemDto(item);
        Item mappedItem = ItemMapper.toItem(dto, owner, request);

        assertEquals(item.getId(), dto.getId());
        assertEquals(request.getId(), dto.getRequestId());
        assertEquals(dto.getName(), mappedItem.getName());
        assertEquals(owner, mappedItem.getOwner());
        assertEquals(request, mappedItem.getRequest());
    }

    @Test
    void shouldCreateBookingAndRequestModels() {
        User user = new User(
                1L,
                "Пользователь",
                "model@mail.ru"
        );

        Item item = new Item(
                1L,
                "Вещь",
                "Описание",
                true,
                user,
                null
        );

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);

        Booking booking = new Booking(
                1L,
                start,
                end,
                item,
                user,
                BookingStatus.WAITING
        );

        ItemRequest request = new ItemRequest(
                1L,
                "Запрос",
                user,
                start
        );

        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(item, booking.getItem());
        assertEquals(user, booking.getBooker());
        assertEquals("Запрос", request.getDescription());
        assertNotNull(new BookingDto());
        assertNotNull(new ItemRequestController());
        assertNotNull(new ItemRequestDto());
    }
}
