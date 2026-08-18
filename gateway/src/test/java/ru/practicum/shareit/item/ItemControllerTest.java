package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void shouldPassItemRequestsToClient() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Дрель", "Простая дрель", true, null);
        CommentDto commentDto = new CommentDto(null, "Отличная вещь", null, null);
        ResponseEntity<Object> response = ResponseEntity.<Object>ok(Map.of("id", 1));
        when(itemClient.create(anyLong(), any())).thenReturn(response);
        when(itemClient.update(anyLong(), anyLong(), any())).thenReturn(response);
        when(itemClient.getById(1, 1)).thenReturn(response);
        when(itemClient.getAllByOwner(1, 0, 10)).thenReturn(response);
        when(itemClient.search(1, "дрель", 0, 10)).thenReturn(response);
        when(itemClient.addComment(anyLong(), anyLong(), any())).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content("{\"name\":\"Новая дрель\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items/1").header(USER_HEADER, 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/items").header(USER_HEADER, 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, 1)
                        .param("text", "дрель"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());

        verify(itemClient).create(anyLong(), any());
        verify(itemClient).update(anyLong(), anyLong(), any());
        verify(itemClient).getById(1, 1);
        verify(itemClient).getAllByOwner(1, 0, 10);
        verify(itemClient).search(1, "дрель", 0, 10);
        verify(itemClient).addComment(anyLong(), anyLong(), any());
    }

    @Test
    void shouldRejectInvalidItemAndPagination() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content("{\"description\":\"Описание\",\"available\":true}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 1)
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content("{\"description\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
