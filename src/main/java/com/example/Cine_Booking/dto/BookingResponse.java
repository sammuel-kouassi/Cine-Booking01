package com.example.Cine_Booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long bookingId,
        String status,
        BigDecimal totalAmount,
        int numberOfSeats,
        LocalDateTime bookingTime
) {}