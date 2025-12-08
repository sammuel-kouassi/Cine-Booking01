package com.example.cine_booking;

import com.example.cine_booking.controller.BookingController;
import com.example.cine_booking.dto.CreateBookingRequest;
import com.example.cine_booking.model.Booking;
import com.example.cine_booking.model.enums.BookingStatus;
import com.example.cine_booking.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Use @MockBean instead of @MockitoBean to avoid inline mock maker on JDK 25
    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    // 1. Simule un utilisateur authentifié pour passer le filtre de sécurité
    @WithMockUser(username = "client", roles = "CUSTOMER")
    void should_Return201_When_BookingIsSuccessful() throws Exception {

        // --- GIVEN ---
        CreateBookingRequest request = new CreateBookingRequest(1L, 100L, List.of(10L, 11L));

        Booking mockBooking = Booking.builder()
                .id(555L)
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("30.00"))
                .bookingTime(LocalDateTime.now())
                .build();

        when(bookingService.createBooking(eq(1L), eq(100L), anyList()))
                .thenReturn(mockBooking);

        // --- WHEN & THEN ---
        mockMvc.perform(post("/api/bookings")
                        .with(csrf()) // 2. Indispensable pour POST/PUT/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(555L))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(30.00));
    }
}