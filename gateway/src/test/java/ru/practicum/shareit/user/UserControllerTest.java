package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    void shouldPassUserRequestsToClient() throws Exception {
        UserDto userDto = new UserDto(null, "Иван", "ivan@example.com");
        ResponseEntity<Object> response = ResponseEntity.<Object>ok(Map.of("id", 1));
        when(userClient.create(any())).thenReturn(response);
        when(userClient.update(anyLong(), any())).thenReturn(response);
        when(userClient.getById(1)).thenReturn(response);
        when(userClient.getAll()).thenReturn(response);
        when(userClient.deleteById(1)).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content("{\"name\":\"Пётр\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userClient).create(any());
        verify(userClient).update(anyLong(), any());
        verify(userClient).getById(1);
        verify(userClient).getAll();
        verify(userClient).deleteById(1);
    }

    @Test
    void shouldRejectInvalidUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"email\":\"wrong\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/users/0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/users/1")
                        .contentType("application/json")
                        .content("{\"email\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }
}
