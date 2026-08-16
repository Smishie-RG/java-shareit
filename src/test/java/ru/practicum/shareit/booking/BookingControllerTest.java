package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("Создание, подтверждение и получение бронирования")
    void shouldCreateApproveAndGetBooking() throws Exception {
        UserDto owner = createUser("Владелец бронирования", "booking-owner@mail.ru");
        UserDto booker = createUser("Арендатор", "booking-booker@mail.ru");
        ItemDto item = createItem(owner.getId(), "Проектор", true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(2);

        BookingDto booking = createBooking(
                booker.getId(), item.getId(), start, end, 200);

        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId())
                        .header(USER_HEADER, owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header(USER_HEADER, booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(item.getId()))
                .andExpect(jsonPath("$.booker.id").value(booker.getId()));

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ошибка при создании некорректного бронирования")
    void shouldRejectInvalidBooking() throws Exception {
        UserDto owner = createUser("Хозяин вещи", "invalid-owner@mail.ru");
        UserDto booker = createUser("Пользователь", "invalid-booker@mail.ru");
        ItemDto available = createItem(owner.getId(), "Доступная вещь", true);
        ItemDto unavailable = createItem(owner.getId(), "Недоступная вещь", false);
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        createBooking(booker.getId(), unavailable.getId(), start, start.plusDays(1), 400);
        createBooking(owner.getId(), available.getId(), start, start.plusDays(1), 404);
        createBooking(booker.getId(), available.getId(), start, start, 400);
        createBooking(
                booker.getId(),
                available.getId(),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                400
        );
        createBooking(booker.getId(), 999999L, start, start.plusDays(1), 404);

        NewBookingDto withoutStart = new NewBookingDto(
                available.getId(), null, start.plusDays(1));
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withoutStart)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание бронирования при небольшой задержке запроса")
    void shouldCreateBookingWithSmallDateDelay() throws Exception {
        UserDto owner = createUser("Владелец задержки", "delay-owner@mail.ru");
        UserDto booker = createUser("Арендатор задержки", "delay-booker@mail.ru");
        ItemDto item = createItem(owner.getId(), "Вещь с задержкой", true);
        LocalDateTime start = LocalDateTime.now().minusNanos(100_000_000);

        createBooking(
                booker.getId(),
                item.getId(),
                start,
                start.plusDays(1),
                200
        );
    }

    @Test
    @DisplayName("Ошибка при неположительных идентификаторах")
    void shouldRejectNonPositiveIds() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", -1)
                        .header(USER_HEADER, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, -1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items/{itemId}", -1)
                        .header(USER_HEADER, 1))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/{userId}", -1))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка при подтверждении бронирования другим пользователем")
    void shouldRejectApprovalByAnotherUser() throws Exception {
        UserDto owner = createUser("Владелец", "approve-owner@mail.ru");
        UserDto booker = createUser("Арендатор", "approve-booker@mail.ru");
        UserDto other = createUser("Другой", "approve-other@mail.ru");
        ItemDto item = createItem(owner.getId(), "Пила", true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingDto booking = createBooking(
                booker.getId(), item.getId(), start, start.plusDays(1), 200);

        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId())
                        .header(USER_HEADER, other.getId())
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId())
                        .header(USER_HEADER, other.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Фильтрация бронирований по состоянию")
    void shouldFilterBookingsByState() throws Exception {
        UserDto owner = createUser("Владелец фильтра", "state-owner@mail.ru");
        UserDto booker = createUser("Арендатор фильтра", "state-booker@mail.ru");
        ItemDto itemDto = createItem(owner.getId(), "Фильтруемая вещь", true);
        User bookerModel = userRepository.findById(booker.getId()).orElseThrow();
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        bookingRepository.saveAll(List.of(
                new Booking(null, now.minusDays(2), now.minusDays(1),
                        item, bookerModel, BookingStatus.APPROVED),
                new Booking(null, now.minusHours(1), now.plusHours(1),
                        item, bookerModel, BookingStatus.APPROVED),
                new Booking(null, now.plusDays(1), now.plusDays(2),
                        item, bookerModel, BookingStatus.APPROVED),
                new Booking(null, now.plusDays(3), now.plusDays(4),
                        item, bookerModel, BookingStatus.WAITING),
                new Booking(null, now.plusDays(5), now.plusDays(6),
                        item, bookerModel, BookingStatus.REJECTED)
        ));

        checkBookingList(booker.getId(), "/bookings", "CURRENT", 1);
        checkBookingList(booker.getId(), "/bookings", "PAST", 1);
        checkBookingList(booker.getId(), "/bookings", "FUTURE", 3);
        checkBookingList(booker.getId(), "/bookings", "WAITING", 1);
        checkBookingList(booker.getId(), "/bookings", "REJECTED", 1);
        checkBookingList(booker.getId(), "/bookings", "ALL", 5);
        checkBookingList(owner.getId(), "/bookings/owner", "ALL", 5);

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, booker.getId())
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавление отзыва после завершённого бронирования")
    void shouldAddCommentAfterCompletedBooking() throws Exception {
        UserDto owner = createUser("Владелец отзыва", "comment-owner@mail.ru");
        UserDto booker = createUser("Автор отзыва", "comment-booker@mail.ru");
        ItemDto itemDto = createItem(owner.getId(), "Дрель с отзывом", true);
        User bookerModel = userRepository.findById(booker.getId()).orElseThrow();
        Item item = itemRepository.findById(itemDto.getId()).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        Booking booking = bookingRepository.save(new Booking(
                null,
                now.minusDays(2),
                now.minusDays(1),
                item,
                bookerModel,
                BookingStatus.APPROVED
        ));

        CommentDto comment = new CommentDto(null, "Отличная дрель", null, null);
        mockMvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header(USER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.text").value("Отличная дрель"))
                .andExpect(jsonPath("$.authorName").value(booker.getName()))
                .andExpect(jsonPath("$.created").exists());

        mockMvc.perform(get("/items/{itemId}", item.getId())
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking.id").value(booking.getId()))
                .andExpect(jsonPath("$.lastBooking.bookerId").value(booker.getId()))
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments.length()").value(1));

        mockMvc.perform(get("/items/{itemId}", item.getId())
                        .header(USER_HEADER, booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments.length()").value(1));
    }

    @Test
    @DisplayName("Ошибка при отзыве до завершения бронирования")
    void shouldRejectCommentBeforeBookingEnd() throws Exception {
        UserDto owner = createUser("Владелец будущего", "future-owner@mail.ru");
        UserDto booker = createUser("Будущий арендатор", "future-booker@mail.ru");
        ItemDto item = createItem(owner.getId(), "Будущая вещь", true);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        BookingDto booking = createBooking(
                booker.getId(), item.getId(), start, start.plusDays(1), 200);

        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId())
                        .header(USER_HEADER, owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk());

        CommentDto comment = new CommentDto(null, "Слишком рано", null, null);
        mockMvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header(USER_HEADER, booker.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
    }

    private UserDto createUser(String name, String email) throws Exception {
        UserDto userDto = new UserDto(null, name, email);
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(response, UserDto.class);
    }

    private ItemDto createItem(long ownerId,
                               String name,
                               boolean available) throws Exception {
        ItemDto itemDto = new ItemDto(
                null,
                name,
                "Описание вещи",
                available,
                null
        );
        String response = mockMvc.perform(post("/items")
                        .header(USER_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readValue(response, ItemDto.class);
    }

    private BookingDto createBooking(long bookerId,
                                     long itemId,
                                     LocalDateTime start,
                                     LocalDateTime end,
                                     int expectedStatus) throws Exception {
        NewBookingDto bookingDto = new NewBookingDto(itemId, start, end);
        String response = mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().is(expectedStatus))
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        if (expectedStatus != 200) {
            return null;
        }
        return objectMapper.readValue(response, BookingDto.class);
    }

    private void checkBookingList(long userId,
                                  String path,
                                  String state,
                                  int size) throws Exception {
        mockMvc.perform(get(path)
                        .header(USER_HEADER, userId)
                        .param("state", state))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(size));
    }
}
