package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateGetAndReturnAllUsers() throws Exception {
        UserDto created = createUser(
                "Иван",
                "ivan-create@mail.ru"
        );

        assertNotNull(created.getId());

        mockMvc.perform(get("/users/{userId}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.email").value("ivan-create@mail.ru"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$[*].id",
                        hasItem(created.getId().intValue())
                ));
    }

    @Test
    void shouldUpdateUserFieldsSeparately() throws Exception {
        UserDto created = createUser(
                "Анна",
                "anna-update@mail.ru"
        );

        UserDto nameUpdate = new UserDto(
                null,
                "Анна Новая",
                null
        );

        mockMvc.perform(patch("/users/{userId}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nameUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Анна Новая"))
                .andExpect(jsonPath("$.email").value("anna-update@mail.ru"));

        UserDto emailUpdate = new UserDto(
                null,
                null,
                "anna-new@mail.ru"
        );

        mockMvc.perform(patch("/users/{userId}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Анна Новая"))
                .andExpect(jsonPath("$.email").value("anna-new@mail.ru"));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        UserDto created = createUser(
                "Пётр",
                "petr-delete@mail.ru"
        );

        mockMvc.perform(delete("/users/{userId}", created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{userId}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidUser() throws Exception {
        UserDto withoutName = new UserDto(
                null,
                "",
                "no-name@mail.ru"
        );

        UserDto withoutEmail = new UserDto(
                null,
                "Без почты",
                null
        );

        UserDto invalidEmail = new UserDto(
                null,
                "Неверная почта",
                "user.com"
        );

        performCreate(withoutName, 400);
        performCreate(withoutEmail, 400);
        performCreate(invalidEmail, 400);
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {
        UserDto first = createUser(
                "Первый",
                "duplicate@mail.ru"
        );

        UserDto second = createUser(
                "Второй",
                "second-duplicate@mail.ru"
        );

        UserDto duplicate = new UserDto(
                null,
                "Дубликат",
                first.getEmail()
        );

        performCreate(duplicate, 409);

        UserDto update = new UserDto(
                null,
                null,
                first.getEmail()
        );

        mockMvc.perform(patch("/users/{userId}", second.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidUserUpdate() throws Exception {
        UserDto created = createUser(
                "Олег",
                "oleg-validation@mail.ru"
        );

        UserDto emptyName = new UserDto(
                null,
                " ",
                null
        );

        mockMvc.perform(patch("/users/{userId}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyName)))
                .andExpect(status().isBadRequest());

        UserDto invalidEmail = new UserDto(
                null,
                null,
                "wrong-email"
        );

        mockMvc.perform(patch("/users/{userId}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(get("/users/{userId}", 999999))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/users/{userId}", 999999))
                .andExpect(status().isNotFound());
    }

    private UserDto createUser(String name, String email) throws Exception {
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

    private void performCreate(
            UserDto userDto,
            int expectedStatus
    ) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().is(expectedStatus));
    }
}