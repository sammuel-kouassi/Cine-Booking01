package com.example.Cine_Booking.dto;

import java.util.List;

public record CreateBookingRequest(
        Long userId,
        Long screeningId,
        List<Long> seatIds
) {}