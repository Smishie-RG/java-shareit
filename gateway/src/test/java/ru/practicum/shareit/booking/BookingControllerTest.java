package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void shouldPassBookingRequestsToClient() throws Exception {
        BookItemRequestDto bookingDto = new BookItemRequestDto(
                1,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        ResponseEntity<Object> response = ResponseEntity.<Object>ok(Map.of("id", 1));
        when(bookingClient.bookItem(anyLong(), any())).thenReturn(response);
        when(bookingClient.approve(1, 1, true)).thenReturn(response);
        when(bookingClient.getBooking(1, 1L)).thenReturn(response);
        when(bookingClient.getBookings(1, BookingState.ALL, 0, 10)).thenReturn(response);
        when(bookingClient.getOwnerBookings(1, BookingState.ALL, 0, 10)).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1)
                        .param("approved", "true"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings/1").header(USER_HEADER, 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings").header(USER_HEADER, 1))
                .andExpect(status().isOk());
        mockMvc.perform(get("/bookings/owner").header(USER_HEADER, 1))
                .andExpect(status().isOk());

        verify(bookingClient).bookItem(anyLong(), any());
        verify(bookingClient).approve(1, 1, true);
        verify(bookingClient).getBooking(1, 1L);
        verify(bookingClient).getBookings(1, BookingState.ALL, 0, 10);
        verify(bookingClient).getOwnerBookings(1, BookingState.ALL, 0, 10);
    }

    @Test
    void shouldRejectInvalidBookingRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1)
                        .param("state", "wrong"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content("{\"itemId\":0}"))
                .andExpect(status().isBadRequest());

        BookItemRequestDto bookingDto = new BookItemRequestDto(
                1,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1));
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }
}
