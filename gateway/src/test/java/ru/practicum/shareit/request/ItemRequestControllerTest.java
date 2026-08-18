package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient requestClient;

    @Test
    void shouldPassRequestOperationsToClient() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto(
                null, "Нужна дрель", null, null);
        ResponseEntity<Object> response = ResponseEntity.<Object>ok(Map.of("id", 1));
        when(requestClient.create(anyLong(), any())).thenReturn(response);
        when(requestClient.getOwnRequests(1)).thenReturn(response);
        when(requestClient.getAllRequests(1, 0, 10)).thenReturn(response);
        when(requestClient.getById(1, 1)).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/requests").header(USER_HEADER, 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/requests/all").header(USER_HEADER, 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/requests/1").header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(requestClient).create(anyLong(), any());
        verify(requestClient).getOwnRequests(1);
        verify(requestClient).getAllRequests(1, 0, 10);
        verify(requestClient).getById(1, 1);
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content("{\"description\":\" \"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1)
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
