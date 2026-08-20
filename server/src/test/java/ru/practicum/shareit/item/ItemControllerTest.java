package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserDto;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateGetAndReturnOwnerItems() throws Exception {
        UserDto owner = createUser(
                "Владелец",
                "owner-create-item@mail.ru"
        );

        ItemDto created = createItem(
                owner.getId(),
                "Дрель",
                "Ударная дрель",
                true
        );

        mockMvc.perform(get("/items/{itemId}", created.getId())
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Ударная дрель"))
                .andExpect(jsonPath("$.available").value(true));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[*].id",
                        hasItem(created.getId().intValue())
                ));
    }

    @Test
    void shouldUpdateItemFieldsSeparately() throws Exception {
        UserDto owner = createUser(
                "Редактор",
                "owner-update-item@mail.ru"
        );

        ItemDto item = createItem(
                owner.getId(),
                "Старая вещь",
                "Старое описание",
                true
        );

        ItemDto nameUpdate = new ItemDto(
                null,
                "Новое имя",
                null,
                null,
                null
        );

        performUpdate(owner.getId(), item.getId(), nameUpdate)
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.description").value("Старое описание"));

        ItemDto descriptionUpdate = new ItemDto(
                null,
                null,
                "Новое описание",
                null,
                null
        );

        performUpdate(owner.getId(), item.getId(), descriptionUpdate)
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.description").value("Новое описание"));

        ItemDto availableUpdate = new ItemDto(
                null,
                null,
                null,
                false,
                null
        );

        performUpdate(owner.getId(), item.getId(), availableUpdate)
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldUpdateAllItemFields() throws Exception {
        UserDto owner = createUser(
                "Полное обновление",
                "full-update@mail.ru"
        );

        ItemDto item = createItem(
                owner.getId(),
                "Стул",
                "Обычный стул",
                true
        );

        ItemDto update = new ItemDto(
                null,
                "Кресло",
                "Мягкое кресло",
                false,
                null
        );

        performUpdate(owner.getId(), item.getId(), update)
                .andExpect(jsonPath("$.name").value("Кресло"))
                .andExpect(jsonPath("$.description").value("Мягкое кресло"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void shouldSearchAvailableItemsIgnoringCase() throws Exception {
        UserDto owner = createUser(
                "Поиск",
                "owner-search@mail.ru"
        );

        createItem(
                owner.getId(),
                "Осциллограф",
                "Точный прибор",
                true
        );

        createItem(
                owner.getId(),
                "Измеритель",
                "Портативный осциллограф",
                true
        );

        createItem(
                owner.getId(),
                "Старый осциллограф",
                "Недоступен",
                false
        );

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, owner.getId())
                        .param("text", "оСцИлЛоГрАф"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptySearchResultForBlankTextAndUnavailableItem()
            throws Exception {
        UserDto owner = createUser(
                "Пустой поиск",
                "empty-search@mail.ru"
        );

        createItem(
                owner.getId(),
                "Уникальный рубанок",
                "Инструмент",
                false
        );

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, owner.getId())
                        .param("text", "рубанок"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, owner.getId())
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldRejectRequestWithoutHeaderAndUnknownUser() throws Exception {
        ItemDto item = new ItemDto(
                null,
                "Молоток",
                "Стальной",
                true,
                null
        );

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isBadRequest());

        performCreate(999999, item, 404);
    }

    @Test
    void shouldRejectUpdateByAnotherUser() throws Exception {
        UserDto owner = createUser(
                "Хозяин",
                "real-owner@mail.ru"
        );

        UserDto other = createUser(
                "Другой",
                "other-user@mail.ru"
        );

        ItemDto item = createItem(
                owner.getId(),
                "Самокат",
                "Городской",
                true
        );

        ItemDto update = new ItemDto(
                null,
                "Чужой самокат",
                null,
                null,
                null
        );

        mockMvc.perform(patch("/items/{itemId}", item.getId())
                        .header(USER_HEADER, other.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnErrorsForUnknownItemAndMissingHeader() throws Exception {
        UserDto owner = createUser(
                "Ошибки",
                "item-errors@mail.ru"
        );

        ItemDto update = new ItemDto(
                null,
                "Новое имя",
                null,
                null,
                null
        );

        mockMvc.perform(get("/items/{itemId}", 999999)
                        .header(USER_HEADER, owner.getId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/items/{itemId}", 999999)
                        .header(USER_HEADER, owner.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/items/{itemId}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 999999))
                .andExpect(status().isNotFound());
    }

    private UserDto createUser(
            String name,
            String email
    ) throws Exception {
        UserDto userDto = new UserDto(null, name, email);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, UserDto.class);
    }

    private ItemDto createItem(
            long userId,
            String name,
            String description,
            boolean available
    ) throws Exception {
        ItemDto itemDto = new ItemDto(
                null,
                name,
                description,
                available,
                null
        );

        String response = mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, ItemDto.class);
    }

    private ResultActions performUpdate(
            long userId,
            long itemId,
            ItemDto itemDto
    ) throws Exception {
        return mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());
    }

    private void performCreate(
            long userId,
            ItemDto itemDto,
            int expectedStatus
    ) throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().is(expectedStatus));
    }
}
